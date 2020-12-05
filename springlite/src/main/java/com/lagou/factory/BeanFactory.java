package com.lagou.factory;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanFactory {

    /**
     * 工程类 的两个任务
     *
     * 任务一、加载 解析xml ，读取想xml 中的bead的 信息，通过反射的技术 实例化bean对象，然后放入容器中 待用
     * 任务二、提供接口方法根据id从 容器中获取 bean （静态方法）
     *
     */

    // 声明一个容器，存储实例化对象
    private static Map<String,Object> map = new HashMap<>();

    // 解析配置文件
    static {
        InputStream resourceAsStream = BeanFactory.class.getClassLoader().getResourceAsStream("beans.xml");
        try {
            // dom4j 加载 配置文件 ,与 xpath 表达式 解析文件
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(resourceAsStream);
            //得到根元素
            Element rootElement = document.getRootElement();

            //获取 根元素下的 所有 子 元素 bean
            List<Element> list = rootElement.selectNodes("//bean");
            // 对每一个 <bean> 做解析工作
            for (int i = 0; i < list.size(); i++) {
                Element element = list.get(i);
                String idStr = element.attributeValue("id");
                String classStr = element.attributeValue("class");

                // 通过反射 实例化对象
                Class<?> aClass = Class.forName(classStr);
                Object o = aClass.newInstance();
                map.put(idStr,o);

            }

            // 获取 根元素下的 所有 子 元素  property
            List<Element> properties = rootElement.selectNodes("//property");

            for (int i = 0; i < properties.size(); i++) {

                // 获取当前的 element 及 它的属性
                Element element = properties.get(i);

                String ref = element.attributeValue("ref");
                String name = element.attributeValue("name");

                // 获取该元素 的父元素
                String parentId = element.getParent().attributeValue("id");

                // 容器中获取 父元素的 实例
                Object  parentObj= map.get(parentId);

                // 变量 父元素实例 的所有方法
                Method[] methods = parentObj.getClass().getMethods();
                for (int j = 0; j < methods.length; j++) {

                    Method method = methods[j];

                    // 父元素方法 有一个 为  set + 本类类名的方法(如：setAccount)时， 执行注入操作。
                    if( ("set"+name).equals(method.getName()) ){
                        Object propertyObj = map.get(ref);
                        method.invoke(parentObj,propertyObj);

                    }
                }
                //重新放入 容器中
                map.put(parentId,parentObj);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 提供一个 外部获取 bean的方法
    public static Object getBean(String id)  {

       return map.get(id);
    }

}
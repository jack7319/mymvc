package com.bizideal.mn.core;

import com.bizideal.mn.annotation.MyAutowired;
import com.bizideal.mn.annotation.MyController;
import com.bizideal.mn.annotation.MyRequestMapping;
import com.bizideal.mn.annotation.MyService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author : liulq
 * @date: 创建时间: 2018/4/11 13:57
 * @version: 1.0
 * @Description:
 */
public class MyDispatcherServlet extends HttpServlet {

    private static Logger logger = LoggerFactory.getLogger(MyDispatcherServlet.class);

    // 所有带有myservice/mycontroller注解的全类名，com.bizideal.mn.service.impl.UserInfoServiceImpl
    private Set<String> classNames = new HashSet<>();

    // 实例化的bean
    private Map<String, Object> singletonObjects = new HashMap<>();

    private Map<String, Object> handlerMapping = new HashMap<>();

    public MyDispatcherServlet() {
        super();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        logger.debug("MyDispatcherServlet init..");
        String packageName = config.getInitParameter("scanPackage");
        scanPackage(packageName);

        doInstance();

        doAutowired();

        doHandlerMapping();
    }

    private void doHandlerMapping() {
        for (Map.Entry<String, Object> entry : singletonObjects.entrySet()) {
            Object controller = entry.getValue();
            Class<?> clazz = controller.getClass();
            if (!clazz.isAnnotationPresent(MyController.class)) {
                // 不是controller
                continue;
            }
            String path = "";
            if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
                // controller有requestMapping注解
                MyRequestMapping annotation = clazz.getAnnotation(MyRequestMapping.class);
                String value1 = annotation.value();
                path += value1;
            }
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                    continue;
                }
                MyRequestMapping annotation = method.getAnnotation(MyRequestMapping.class);
                String value = annotation.value();
            }
        }
    }

    private void doAutowired() {
        for (Map.Entry<String, Object> entry : singletonObjects.entrySet()) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(MyAutowired.class)) {
                    continue;
                }
                MyAutowired annotation = field.getAnnotation(MyAutowired.class);
                String wantedBeanName = annotation.value().trim();
                String beanName = StringUtils.isBlank(wantedBeanName) ? lowerFirstChar(field.getType().getSimpleName()) : wantedBeanName;
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                if (singletonObjects.containsKey(beanName)) {
                    try {
                        field.set(entry.getValue(), singletonObjects.get(beanName));
                    } catch (IllegalAccessException e) {
                        logger.error("自动注入失败...", e);
                    } finally {
                        field.setAccessible(accessible);
                    }
                } else if ("".equals(wantedBeanName)) {
                    try {
                        field.set(entry.getValue(), getByNameOrType(beanName, field.getType()));
                    } catch (IllegalAccessException e) {
                        logger.error("自动注入失败...", e);
                    } finally {
                        field.setAccessible(accessible);
                    }
                }

            }

        }
    }

    public Object getByNameOrType(String beanName, Class clazz) {
        if (singletonObjects.containsKey(beanName)) {
            return singletonObjects.get(beanName);
        }
        for (Map.Entry<String, Object> entry : singletonObjects.entrySet()) {
            Object value = entry.getValue();
            if (clazz.isAssignableFrom(value.getClass())) {
                return value;
            }
        }
        return null;
    }

    private void doInstance() {
        for (String className : classNames) {
            try {
                String beanName = "";
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyController.class)) {
                    MyController c = clazz.getAnnotation(MyController.class);
                    String value = c.value();
                    beanName = StringUtils.isBlank(value) ? lowerFirstChar(clazz.getSimpleName()) : value.trim();
                } else if (clazz.isAnnotationPresent(MyService.class)) {
                    MyService s = clazz.getAnnotation(MyService.class);
                    String value = s.value();
                    Class<?>[] interfaces = clazz.getInterfaces();
                    beanName = StringUtils.isBlank(value) ? lowerFirstChar(interfaces[0].getSimpleName()) : value.trim();
                }
                singletonObjects.put(beanName, clazz.newInstance());
            } catch (Exception e) {
                logger.error("实例化失败...", e);
            }
        }
    }

    private String lowerFirstChar(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    // 扫描所有的自定义controller和service
    private void scanPackage(String packageName) {
        String path = packageName.replaceAll("\\.", "/");
        URL url = getClass().getClassLoader().getResource(path);
        File file = new File(url.getFile());
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                scanPackage(packageName + "." + f.getName());
            } else {
                if (!f.getName().endsWith(".class")) {
                    continue;
                }
                String className = packageName + "." + f.getName().replace(".class", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(MyController.class) || clazz.isAnnotationPresent(MyService.class)) {
                        classNames.add(className);
                    }
                } catch (ClassNotFoundException e) {
                    logger.error("包扫描失败...", e);
                }

            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        out(resp, "11111");
    }

    private void out(HttpServletResponse response, String str) {
        try {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

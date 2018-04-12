package com.bizideal.mn.core;

import com.bizideal.mn.annotation.*;
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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

    private Map<String, HandlerEntity> handlerMapping = new HashMap<>();

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

    private class HandlerEntity {
        Object controllerObj;
        Method method;
        Map<String, Integer> paramsMap;

        public HandlerEntity(Object controllerObj, Method method, Map<String, Integer> paramsMap) {
            this.controllerObj = controllerObj;
            this.method = method;
            this.paramsMap = paramsMap;
        }
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
                String requestPath = path + "/" + value;
                requestPath = requestPath.replaceAll("/+", "/");
                // 获取所有的参数
                String[] params = Play.getMethodParameterNamesByAsm4(clazz, method);
                // 获取参数注解
                Annotation[][] annotations = method.getParameterAnnotations();
                // 方法的参数类型
                Class<?>[] parameterTypes = method.getParameterTypes();
                Map<String, Integer> paramMap = new HashMap<>();
                for (int i = 0; i < annotations.length; i++) {
                    Annotation[] a = annotations[i];
                    if (a.length == 0) {
                        // 没有注解
                        Class<?> parameterType = parameterTypes[i];
                        if (parameterType == HttpServletRequest.class || parameterType == HttpServletResponse.class) {
                            paramMap.put(parameterType.getName(), i);
                        } else {
                            paramMap.put(params[i], i);
                        }
                        continue;
                    }
                    // 有注解
                    for (Annotation an : a) {
                        if (an.annotationType() == MyRequestParam.class) {
                            String value1 = ((MyRequestParam) an).value();
                            if (!"".equals(value1.trim())) {
                                paramMap.put(value1, i);
                            } else {
                                // 没有指定value，还是用取出的param
                                paramMap.put(params[i], i);
                            }
                        }
                    }
                    HandlerEntity handlerEntity = new HandlerEntity(controller, method, paramMap);
                    handlerMapping.put(requestPath, handlerEntity);
                }
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
        doInvoke(req, resp);
    }

    private void doInvoke(HttpServletRequest req, HttpServletResponse resp) {
        String requestURI = req.getRequestURI();
        if (handlerMapping.containsKey(requestURI)) {
            HandlerEntity o = handlerMapping.get(requestURI);
            Class<?>[] parameterTypes = o.method.getParameterTypes();
            try {
                Map<String, Integer> paramsMap = o.paramsMap;
                Object[] params = new Object[paramsMap.size()];
                for (Map.Entry<String, Integer> entry : paramsMap.entrySet()) {
                    String paramName = entry.getKey();
                    if (paramName.equals(HttpServletRequest.class.getName())) {
                        params[entry.getValue()] = req;
                    } else if (paramName.equals(HttpServletResponse.class.getName())) {
                        params[entry.getValue()] = resp;
                    } else {
                        String parameter = req.getParameter(paramName);
                        if (null != parameter) {
                            params[entry.getValue()] = convert(parameter, parameterTypes[entry.getValue()]);
                        }
                    }
                }
                Object invoke = o.method.invoke(o.controllerObj, params);
            } catch (Exception e) {
                logger.error("调用出错..", e);
            }
        } else {
            resp.setStatus(404);
        }
    }

    /**
     * 将用户传来的参数转换为方法需要的参数类型
     */
    private Object convert(String parameter, Class<?> targetType) {
        if (targetType == String.class) {
            return parameter;
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.valueOf(parameter);
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.valueOf(parameter);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            if (parameter.toLowerCase().equals("true") || parameter.equals("1")) {
                return true;
            } else if (parameter.toLowerCase().equals("false") || parameter.equals("0")) {
                return false;
            }
            throw new RuntimeException("不支持的参数");
        } else {
            return null;
        }
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

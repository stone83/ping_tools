package com.jj.game.boost.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ReflectUtils {

    /**
     * 是否是公用静态方法
     *
     * @param member
     * @return
     */
    @SuppressWarnings({"JavaDoc", "unused"})
    private static boolean isPublicStatic(Member member) {
        int mod = member.getModifiers();
        return Modifier.isPublic(mod) && Modifier.isStatic(mod);
    }

    @SuppressWarnings("unused")
    private static boolean isPublic(Member member) {
        int mod = member.getModifiers();
        return Modifier.isPublic(mod);
    }

    private static boolean isStatic(Member member) {
        int mod = member.getModifiers();
        return Modifier.isStatic(mod);
    }

    /**
     * 获取字段值
     *
     * @param obj
     * @param fieldName
     * @return
     */
    @SuppressWarnings({"JavaDoc", "unused", "TryWithIdenticalCatches"})
    public static Object obtainFieldValue(Object obj, String fieldName) {
        Class<?> cla = obj.getClass();
        try {
            Field field = cla.getDeclaredField(fieldName);
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            if (Modifier.isStatic(field.getModifiers())) {
                return field.get(null);
            }
            return field.get(obj);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据指定起始字符串作为开头,获取匹配的静态字段的整形值的集合.
     *
     * @param cla
     * @param startsWith
     * @return
     */
    @SuppressWarnings({"JavaDoc", "unused"})
    public static List<Object> obtainStaticFieldValues(Class<?> cla, String startsWith) {
        List<Object> result = null;
        try {
            Field[] fs = cla.getDeclaredFields();
            if (fs != null) {
                result = new ArrayList<>();
                Field f;
                for (Field f1 : fs) {
                    f = f1;
                    f.setAccessible(true);
                    if (f.getName().startsWith(startsWith) && isStatic(f)) {
                        result.add(f.get(null));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    @SuppressWarnings({"TryWithIdenticalCatches", "unused"})
    public static Object obtainStaticFieldValue(Class<?> cla, String fieldName) {
        Object result = null;
        try {
            Field f = cla.getDeclaredField(fieldName);
            if (f != null) {
                f.setAccessible(true);
                if (f.getName().equals(fieldName) && isStatic(f)) {
                    result = f.get(null);
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    @SuppressWarnings("unused")
    public static List<Object> obtainNonStaticFieldValues(Object obj, String startsWith) {
        List<Object> result = null;
        Class<?> cla = obj.getClass();
        try {
            Field[] fs = cla.getDeclaredFields();
            if (fs != null) {
                result = new ArrayList<>();
                Field f;
                for (Field f1 : fs) {
                    f = f1;
                    f.setAccessible(true);
                    if (f.getName().startsWith(startsWith) && !isStatic(f)) {
                        result.add(f.get(obj));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    @SuppressWarnings({"TryWithIdenticalCatches", "unused"})
    public static Object obtainNonStaticFieldValue(Object obj, String fieldName) {
        Object result = null;
        Class<?> cla = obj.getClass();
        try {
            Field f = cla.getDeclaredField(fieldName);
            if (f != null) {
                f.setAccessible(true);
                if (f.getName().equals(fieldName) && !isStatic(f)) {
                    result = f.get(obj);
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    @SuppressWarnings("WeakerAccess")
    public static <T> Method obtainMethod(Class<T> cla, String methodName) {
        Method[] ms = cla.getDeclaredMethods();
        for (Method m : ms) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    public static Method obtainMethod(Object obj, String methodName) {
        Class<?> cla = obj.getClass();
        return obtainMethod(cla, methodName);
    }

    /**
     * 调用静态方法
     *
     * @param cla
     * @param name
     * @param parameterTypes
     * @param parameterValues
     * @return
     */
    @SuppressWarnings({"JavaDoc", "unused", "TryWithIdenticalCatches"})
    public static Object invokeStaticMethod(Class<?> cla, String name, Class[] parameterTypes,
                                            Object... parameterValues) {
        try {
            Method method;
            if (parameterTypes == null) {
                method = cla.getDeclaredMethod(name);
            } else {
                method = cla.getDeclaredMethod(name, parameterTypes);
            }
            method.setAccessible(true);
            return method.invoke(null, parameterValues);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            LogUtil.i("e -> " + e);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            LogUtil.i("e -> " + e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            LogUtil.i("e -> " + e);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.i("e -> " + e);
        }
        return null;
    }

    /**
     * 调用非静态方法
     *
     * @param obj
     * @param parameterValues
     * @param parameterTypes
     * @param parameterValues
     * @return
     */
    @SuppressWarnings({"JavaDoc", "WeakerAccess"})
    public static Object invokeNonStaticMethod(Object obj, String methodName, Class[] parameterTypes,
                                               Object... parameterValues) {
        try {
            Method method;
            if (parameterTypes == null) {
                method = obj.getClass().getDeclaredMethod(methodName);
            } else {
                method = obj.getClass().getDeclaredMethod(methodName, parameterTypes);
            }
            method.setAccessible(true);
            if (parameterValues == null) {
                return method.invoke(obj);
            } else {
                return method.invoke(obj, parameterValues);
            }
        } catch (Exception ex) {
//            throw new RuntimeException(ex);
            return null;
        }
    }

    /**
     * @param obj
     * @param simpleName
     * @return
     */
    @SuppressWarnings({"JavaDoc", "unused"})
    public static Class<?> obtainInnerClass(Object obj, String simpleName) {
        Class<?> cla = obj.getClass();
        Class<?>[] cs = cla.getDeclaredClasses();
        for (Class<?> c : cs) {
//            LogUtil.i("cs[i] -> " + cs[i]);
//            LogUtil.i("cs[i].getName() -> " + cs[i].getName());
//            LogUtil.i("cs[i].getSimpleName() -> " + cs[i].getSimpleName());
            if (c.getSimpleName().equals(simpleName)) {
                return c;
            }
        }
        return null;
    }

}

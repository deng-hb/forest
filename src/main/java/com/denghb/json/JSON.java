package com.denghb.json;

import com.denghb.utils.ReflectUtils;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 对象 JSON 解析器
 * TODO 基本类型、数组、某些格式的日期待完善
 */
public class JSON {

    public static String toJSON(Object object) {
        if (null == object) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        toJSON(object, sb, "yyyy-MM-dd HH:mm:ss");
        return sb.toString();
    }

    public static String toJSON(Object object, String dateFormat) {
        if (null == object) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        toJSON(object, sb, dateFormat);
        return sb.toString();
    }

    private static void toJSON(Object object, StringBuilder sb, String dateFormat) {

        if (object instanceof Map) {
            sb.append('{');
            Map map = (Map) object;
            boolean a = false;

            for (Object key : map.keySet()) {
                if (a) {
                    sb.append(',');
                }
                a = true;
                sb.append('"');
                sb.append(key);
                sb.append("\":");
                toJSON(map.get(key), sb, dateFormat);
            }
            sb.append("}");

        } else if (object instanceof List) {

            List list = (List) object;

            sb.append('[');
            boolean a = false;

            for (Object value : list) {
                if (a) {
                    sb.append(',');
                }
                a = true;
                toJSON(value, sb, dateFormat);
            }
            sb.append(']');

        } else {
            if (object instanceof Date) {
                sb.append('"');
                sb.append(format(dateFormat, (Date) object));
                sb.append('"');
            } else if (object instanceof CharSequence) {
                sb.append('"');
                // fix 换行符
                String str = (String)object;
                sb.append(str.replaceAll("\\n","\\\\n"));
                sb.append('"');
            } else if (object instanceof Number) {
                sb.append(object);
            } else {
                List<Field> fields = ReflectUtils.getFields(object.getClass());

                sb.append('{');
                boolean a = false;
                for (Field field : fields) {
                    if (a) {
                        sb.append(',');
                    }
                    a = true;

                    sb.append('"');
                    sb.append(field.getName());
                    sb.append("\":");

                    Object value = ReflectUtils.getFieldValue(field, object);
                    if (null == value) {
                        sb.append(value);
                        continue;
                    }
                    toJSON(value, sb, dateFormat);

                }
                sb.append("}");
            }
        }
    }

    /**
     * {"",""}
     */
    public static <T> T parseJSON(Class<T> clazz, String json) {

        if (null == json) {
            return null;
        }

        Map map = new HashMap();
        initParse(json, null, map);
        if (clazz == Map.class) {
            return (T) map;
        }

        if (!map.isEmpty()) {
            return map2Object(clazz, map);
        }

        return null;
    }

    /**
     * ["",""]
     */
    public static <T> List<T> parseArrayJSON(Class<T> clazz, String json) {

        if (null == json) {
            return null;
        }
        List<Object> list = new ArrayList<Object>();
        initParse(json, list, null);

        if (clazz == Map.class) {
            return (List<T>) list;
        }
        List<T> myList = new ArrayList<T>();

        for (Object object : list) {
            if (object instanceof Map) {
                Map map = (Map) object;
                myList.add(map2Object(clazz, map));
            } else {
                myList.add((T) object);
            }
        }

        return myList;
    }

    private static <T> void initParse(String json, List<Object> list, Map map) {

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {// 解析对象
                if (null == map) {
                    return;
                }
                i = readMap(json, i, map);
            } else if (c == '[') {// 解析集合
                if (null == list) {
                    return;
                }
                i = readList(json, i, list);
            }
        }
    }

    public static <T> T map2Object(Class<T> clazz, Map map) {

        Object object = ReflectUtils.constructorInstance(clazz);
        List<Field> fields = ReflectUtils.getFields(clazz);
        for (Field field : fields) {
            Object value = map.get(field.getName());
            if (null == value) {
                continue;
            }

            Class type = field.getType();
            if (type == List.class) {
                Class tClass = null;
                try {
                    // 获取范型对应的类型 Ljava/util/List<Lcom/denghb/json/JSON$Book;>;
                    Field privateField = Field.class.getDeclaredField("signature");
                    privateField.setAccessible(true);
                    String fieldValue = (String) privateField.get(field);//获得私有字段值

                    if (null != fieldValue) {// 没有明确类型
                        String t = fieldValue.substring(fieldValue.indexOf("<") + 2, fieldValue.length() - 3);
                        t = t.replaceAll("\\/", "\\.");
                        tClass = Class.forName(t);
                    }
//                    System.out.println(t);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                List list = (List) value;
                for (int i = 0; i < list.size(); i++) {
                    Object obj = list.get(i);
                    if (obj instanceof Map) {// 替换对象
                        list.set(i, map2Object(tClass, (Map) obj));
                    }
                }
                ReflectUtils.setFieldValue(field, object, list);
            } else if (type == String.class || type == Map.class) {
                // 字符串 || map
                ReflectUtils.setFieldValue(field, object, value);
            } else if (type == Date.class) {
                // 日期
                Date date = parseStringToDate(String.valueOf(value));
                ReflectUtils.setFieldValue(field, object, date);
            } else if (type.getSuperclass() == Number.class) {
                // 数字
                value = ReflectUtils.constructorInstance(type, String.class, String.valueOf(value));
                ReflectUtils.setFieldValue(field, object, value);
            } else if (type.getName().startsWith("[L")) {
                // TODO 数组
                /* List list = (List) value;
                Object array = Array.newInstance(type, list.size());
                for (int i = 0;i < list.size();i++) {
                    Array.set(array, i, list.get(i));
                }
                ReflectUtils.setFieldValue(field, object, array);
                */
            } else if (type.isPrimitive()) {
                // TODO 基本值类型
//                ReflectUtils.setFieldValue(field, object, value);

            } else {

                // TODO 未知类型
                ReflectUtils.setFieldValue(field, object, value);
            }

        }

        return (T) object;
    }

    // 预测日期字符串然后转换日期
    private static Date parseStringToDate(String date) {

        String parse = date;
        parse = parse.replaceFirst("^[0-9]{4}([^0-9])", "yyyy$1");
        parse = parse.replaceFirst("^[0-9]{2}([^0-9])", "yy$1");
        parse = parse.replaceFirst("([^0-9])[0-9]{1,2}([^0-9])", "$1MM$2");
        parse = parse.replaceFirst("([^0-9])[0-9]{1,2}( ?)", "$1dd$2");
        parse = parse.replaceFirst("( )[0-9]{1,2}([^0-9])", "$1HH$2");
        parse = parse.replaceFirst("([^0-9])[0-9]{1,2}([^0-9])", "$1mm$2");
        parse = parse.replaceFirst("([^0-9])[0-9]{1,2}([^0-9]?)", "$1ss$2");


        return parse(parse, date);
    }

    private static String format(String pattern, Date date) {
        DateFormat format = new SimpleDateFormat(pattern);

        return format.format(date);
    }

    private static Date parse(String pattern, String dateStr) {
        DateFormat format = new SimpleDateFormat(pattern);

        Date date = null;
        try {
            date = format.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 读取集合
     */
    private static int readList(String origin, int index, List list) {

        int length = origin.length();
        while (length >= index) {

            char c = origin.charAt(index++);
            if (c == ']') {
                break;
            }

            if (c == '"') {
                // 基本类型的集合
                StringBuilder sb = new StringBuilder();
                index = read(origin, index, '"', sb);
                list.add(sb.toString());
            } else if (c == '{') {
                // 对象集合
                Map map = new HashMap();
                index = readMap(origin, index, map);
                list.add(map);
            }
        }
        return index;
    }

    /**
     * 读取map
     */
    private static int readMap(String origin, int index, Map map) {
        int length = origin.length();
        dd:
        while (length >= index) {

            char c = origin.charAt(index++);
            if (c == '}') {
                break;
            }
            if (c == '"') {
                StringBuilder key = new StringBuilder();
                index = read(origin, index, '"', key);

                StringBuilder tmp = new StringBuilder();
                while (length >= index) {
                    c = origin.charAt(index++);
                    if (c == '"') {
                        StringBuilder value = new StringBuilder();
                        index = read(origin, index, '"', value);
                        map.put(key.toString(), value.toString());
                        break;
                    }
                    if (c == '{') {
                        Map map2 = new HashMap();
                        index = readMap(origin, index, map2);
                        map.put(key.toString(), map2);
                        break;
                    }
                    if (c == '[') {
                        List list = new ArrayList();
                        index = readList(origin, index, list);
                        map.put(key.toString(), list);
                        break;
                    }

                    // 终止最外层循环
                    if (c == '}') {
                        break dd;
                    }
                    // 数字 || null
                    if (c == ',') {
                        String t = tmp.toString();
                        if ("null".equals(t)) {
                            map.put(key.toString(), null);
                        } else {
                            map.put(key.toString(), t);
                        }
                        break;
                    }
                    if (c != ' ' && c != ':') {
                        tmp.append(c);
                    }
                }

            }

        }
        return index;
    }

    /**
     * 读取字符串 "name" -> name
     */
    private static int read(String origin, int index, char end, StringBuilder sb) {
        int length = origin.length();
        while (length >= index) {
            char c = origin.charAt(index++);
            if (c == end) {
                break;
            }
            sb.append(c);
        }
        return index;
    }

}

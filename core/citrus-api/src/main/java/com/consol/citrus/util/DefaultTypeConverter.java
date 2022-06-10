package com.consol.citrus.util;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import com.consol.citrus.CitrusSettings;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.xml.StringSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamSource;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;

/**
 * @author Christoph Deppisch
 */
public class DefaultTypeConverter implements TypeConverter {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultTypeConverter.class);

    @Override
    public final <T> T convertIfNecessary(Object target, Class<T> type) {
        if (type.isInstance(target)) {
            return type.cast(target);
        }

        T result = convertBefore(target, type);
        if (result != null) {
            return result;
        }

        if (Source.class.isAssignableFrom(type)) {
            if (target.getClass().isAssignableFrom(String.class)) {
                return (T) new StringSource(String.valueOf(target));
            } else if (target.getClass().isAssignableFrom(Node.class)) {
                return (T) new DOMSource((Node) target);
            } else if (target.getClass().isAssignableFrom(InputStreamSource.class)) {
                try {
                    return (T) new StreamSource(((InputStreamSource)target).getInputStream());
                } catch (IOException e) {
                    LOG.warn("Failed to create stream source from object", e);
                }
            }
        }

        if (Map.class.isAssignableFrom(type)) {
            String mapString = String.valueOf(target);

            Properties props = new Properties();
            try {
                props.load(new StringReader(mapString.substring(1, mapString.length() - 1).replaceAll(",\\s*", "\n")));
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to reconstruct object of type map", e);
            }
            Map<String, Object> map = new LinkedHashMap<>();
            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                map.put(entry.getKey().toString(), entry.getValue());
            }

            return (T) map;
        }

        if (String[].class.isAssignableFrom(type)) {
            String arrayString = String.valueOf(target).replaceAll("^\\[", "").replaceAll("]$", "").replaceAll(",\\s", ",");
            return (T) StringUtils.commaDelimitedListToStringArray(arrayString);
        }

        if (List.class.isAssignableFrom(type)) {
            String listString = String.valueOf(target).replaceAll("^\\[", "").replaceAll("]$", "").replaceAll(",\\s", ",");
            return (T) Arrays.asList(StringUtils.commaDelimitedListToStringArray(listString));
        }

        if (byte[].class.isAssignableFrom(type)) {
            if (target instanceof String) {
                try {
                    return (T) String.valueOf(target).getBytes(CitrusSettings.CITRUS_FILE_ENCODING);
                } catch (UnsupportedEncodingException e) {
                    return (T) String.valueOf(target).getBytes();
                }
            } else if (target instanceof ByteBuffer) {
                return (T) ((ByteBuffer) target).array();
            } else if (target instanceof ByteArrayInputStream) {
                try {
                    return (T) StreamUtils.copyToByteArray((ByteArrayInputStream) target);
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to convert input stream to byte[]");
                }
            }
        }

        if (InputStream.class.isAssignableFrom(type)) {
            if (target instanceof InputStream) {
                return (T) target;
            } else if (target instanceof byte[]) {
                return (T) new ByteArrayInputStream((byte[]) target);
            } else if (target instanceof String) {
                try {
                    return (T) new ByteArrayInputStream(String.valueOf(target).getBytes(CitrusSettings.CITRUS_FILE_ENCODING));
                } catch (UnsupportedEncodingException e) {
                    return (T) new ByteArrayInputStream(String.valueOf(target).getBytes());
                }
            } else {
                try {
                    return (T) new ByteArrayInputStream(target.toString().getBytes(CitrusSettings.CITRUS_FILE_ENCODING));
                } catch (UnsupportedEncodingException e) {
                    return (T) new ByteArrayInputStream(target.toString().getBytes());
                }
            }
        }

        if (type.equals(String.class)) {
            if (target == null) {
                return (T) "null";
            } else if (ByteBuffer.class.isAssignableFrom(target.getClass())) {
                return (T) new String(((ByteBuffer) target).array());
            } else if (short[].class.isAssignableFrom(target.getClass())) {
                return (T) Arrays.toString((short[]) target);
            } else if (int[].class.isAssignableFrom(target.getClass())) {
                return (T) Arrays.toString((int[]) target);
            } else if (long[].class.isAssignableFrom(target.getClass())) {
                return (T) Arrays.toString((long[]) target);
            } else if (float[].class.isAssignableFrom(target.getClass())) {
                return (T) Arrays.toString((float[]) target);
            } else if (double[].class.isAssignableFrom(target.getClass())) {
                return (T) Arrays.toString((double[]) target);
            } else if (char[].class.isAssignableFrom(target.getClass())) {
                return (T) Arrays.toString((char[]) target);
            } else if (boolean[].class.isAssignableFrom(target.getClass())) {
                return (T) Arrays.toString((boolean[]) target);
            } else if (Object[].class.isAssignableFrom(target.getClass())) {
                return (T) Arrays.toString((Object[]) target);
            } else if (String[].class.isAssignableFrom(target.getClass())) {
                return (T) Arrays.toString((String[]) target);
            } else if (List.class.isAssignableFrom(target.getClass()) || Map.class.isAssignableFrom(target.getClass())) {
                return (T) target.toString();
            } else if (byte[].class.isAssignableFrom(target.getClass())) {
                return (T) Arrays.toString((byte[]) target);
            }
        }

        if (target instanceof String) {
            try {
                return convertStringToType(String.valueOf(target), type);
            } catch (CitrusRuntimeException e) {
                LOG.warn(String.format("Unable to convert String object to type '%s' - try fallback strategies", type), e);
            }
        }

        if (target instanceof Number) {
            if (type.isAssignableFrom(int.class) || type.isAssignableFrom(Integer.class)) {
                return (T) new Integer(((Number) target).intValue());
            } else if (type.isAssignableFrom(short.class) || type.isAssignableFrom(Short.class)) {
                return (T) new Short(((Number) target).shortValue());
            }  else if (type.isAssignableFrom(byte.class) || type.isAssignableFrom(Byte.class)) {
                return (T) new Byte(((Number) target).byteValue());
            }  else if (type.isAssignableFrom(long.class) || type.isAssignableFrom(Long.class)) {
                return (T) new Long(((Number) target).longValue());
            } else if (type.isAssignableFrom(float.class) || type.isAssignableFrom(Float.class)) {
                return (T) new Float(((Number) target).floatValue());
            } else if (type.isAssignableFrom(double.class) || type.isAssignableFrom(Double.class)) {
                return (T) new Double(((Number) target).doubleValue());
            }
        }

        try {
            return convertAfter(target, type);
        } catch (Exception e) {
            if (String.class.equals(type)) {
                LOG.warn(String.format("Using default toString representation because object type conversion failed with: %s", e.getMessage()));
                return (T) target.toString();
            }

            throw e;
        }
    }

    @Override
    public <T> T convertStringToType(String value, Class<T> type) {
        if (type.isAssignableFrom(String.class)) {
            return (T) value;
        } else if (type.isAssignableFrom(int.class) || type.isAssignableFrom(Integer.class)) {
            return (T) Integer.valueOf(value);
        } else if (type.isAssignableFrom(short.class) || type.isAssignableFrom(Short.class)) {
            return (T) Short.valueOf(value);
        }  else if (type.isAssignableFrom(byte.class) || type.isAssignableFrom(Byte.class)) {
            return (T) Byte.valueOf(value);
        }  else if (type.isAssignableFrom(long.class) || type.isAssignableFrom(Long.class)) {
            return (T) Long.valueOf(value);
        } else if (type.isAssignableFrom(boolean.class) || type.isAssignableFrom(Boolean.class)) {
            return (T) Boolean.valueOf(value);
        } else if (type.isAssignableFrom(float.class) || type.isAssignableFrom(Float.class)) {
            return (T) Float.valueOf(value);
        } else if (type.isAssignableFrom(double.class) || type.isAssignableFrom(Double.class)) {
            return (T) Double.valueOf(value);
        }

        throw new CitrusRuntimeException(String.format("Unable to convert '%s' to required type '%s'", value, type.getName()));
    }

    protected <T> T convertBefore(Object target, Class<T> type) {
        return null;
    }

    /**
     * Subclasses may add additional conversion logic in this method. This is only consulted as a fallback
     * if none of the default conversion strategies did succeed.
     * @param target
     * @param type
     * @param <T>
     * @return
     */
    protected <T> T convertAfter(Object target, Class<T> type) {
        if (String.class.equals(type)) {
            LOG.warn(String.format("Using default toString representation for object type %s", target.getClass()));
            return (T) target.toString();
        }

        throw new CitrusRuntimeException(String.format("Unable to convert object '%s' to target type '%s'",
                Optional.ofNullable(target).map(Object::getClass).map(Class::getName).orElse("null"), type));
    }
}

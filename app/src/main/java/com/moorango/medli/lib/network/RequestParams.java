package com.moorango.medli.lib.network;

/*
 * Android Asynchronous Http Client Copyright (c) 2011 James Smith <james@loopj.com>
 * http://loopj.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class RequestParams {

    public final static String APPLICATION_OCTET_STREAM = "application/octet-stream";

    protected final static String LOG_TAG = "RequestParams";
    protected final ConcurrentHashMap<String, String> urlParams = new ConcurrentHashMap();
    protected final ConcurrentHashMap<String, StreamWrapper> streamParams = new ConcurrentHashMap();
    protected final ConcurrentHashMap<String, FileWrapper> fileParams = new ConcurrentHashMap();
    protected final ConcurrentHashMap<String, Object> urlParamsWithObjects = new ConcurrentHashMap();
    protected boolean isRepeatable;
    protected boolean useJsonStreamer;
    protected boolean autoCloseInputStreams;
    protected String contentEncoding = HTTP.UTF_8;

    /**
     * Constructs a new empty {@code RequestParams} instance.
     */
    public RequestParams() {
        this((Map<String, String>) null);
    }

    /**
     * Constructs a new RequestParams instance containing the key/value string params from the
     * specified map.
     *
     * @param source the source key/value string map to add.
     */
    public RequestParams(Map<String, String> source) {
        if (source != null) {
            for (Map.Entry<String, String> entry : source.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Constructs a new RequestParams instance and populate it with a single initial key/value string
     * param.
     *
     * @param key   the key name for the intial param.
     * @param value the value string for the initial param.
     */
    public RequestParams(final String key, final String value) {
        this(new WeakHashMap<String, String>() {
            {
                put(key, value);
            }
        });
    }

    /**
     * Constructs a new RequestParams instance and populate it with multiple initial key/value string
     * param.
     *
     * @param keysAndValues a sequence of keys and values. Objects are automatically converted to
     *                      Strings (including the value {@code null}).
     * @throws IllegalArgumentException if the number of arguments isn't even.
     */
    public RequestParams(Object... keysAndValues) {
        int len = keysAndValues.length;
        if (len % 2 != 0) throw new IllegalArgumentException("Supplied arguments must be even");
        for (int i = 0; i < len; i += 2) {
            String key = String.valueOf(keysAndValues[i]);
            String val = String.valueOf(keysAndValues[i + 1]);
            put(key, val);
        }
    }

    /**
     * Sets content encoding for return value of {@link #getParamString()} and
     * {@link #createFormEntity()}
     * <p>
     * &nbsp;
     * </p>
     * Default encoding is "UTF-8"
     *
     * @param encoding String constant from {@link org.apache.http.protocol.HTTP}
     */
    public void setContentEncoding(final String encoding) {
        if (encoding != null)
            this.contentEncoding = encoding;
        else
            Log.d(LOG_TAG, "setContentEncoding called with null attribute");
    }

    /**
     * Adds a key/value string pair to the request.
     *
     * @param key   the key name for the new param.
     * @param value the value string for the new param.
     */
    public void put(String key, String value) {
        if (key != null && value != null) {
            urlParams.put(key, value);
        }
    }

    /**
     * Adds a file to the request.
     *
     * @param key  the key name for the new param.
     * @param file the file to add.
     * @throws java.io.FileNotFoundException throws if wrong File argument was passed
     */
    public void put(String key, File file) throws FileNotFoundException {
        put(key, file, null);
    }

    /**
     * Adds a file to the request.
     *
     * @param key         the key name for the new param.
     * @param file        the file to add.
     * @param contentType the content type of the file, eg. application/json
     * @throws java.io.FileNotFoundException throws if wrong File argument was passed
     */
    public void put(String key, File file, String contentType) throws FileNotFoundException {
        if (file == null || !file.exists()) {
            throw new FileNotFoundException();
        }
        if (key != null) {
            fileParams.put(key, new FileWrapper(file, contentType));
        }
    }

    /**
     * Adds an input stream to the request.
     *
     * @param key    the key name for the new param.
     * @param stream the input stream to add.
     */
    public void put(String key, InputStream stream) {
        put(key, stream, null);
    }

    /**
     * Adds an input stream to the request.
     *
     * @param key    the key name for the new param.
     * @param stream the input stream to add.
     * @param name   the name of the stream.
     */
    public void put(String key, InputStream stream, String name) {
        put(key, stream, name, null);
    }

    /**
     * Adds an input stream to the request.
     *
     * @param key         the key name for the new param.
     * @param stream      the input stream to add.
     * @param name        the name of the stream.
     * @param contentType the content type of the file, eg. application/json
     */
    public void put(String key, InputStream stream, String name, String contentType) {
        put(key, stream, name, contentType, autoCloseInputStreams);
    }

    /**
     * Adds an input stream to the request.
     *
     * @param key         the key name for the new param.
     * @param stream      the input stream to add.
     * @param name        the name of the stream.
     * @param contentType the content type of the file, eg. application/json
     * @param autoClose   close input stream automatically on successful upload
     */
    public void put(String key, InputStream stream, String name, String contentType, boolean autoClose) {
        if (key != null && stream != null) {
            streamParams.put(key, StreamWrapper.newInstance(stream, name, contentType, autoClose));
        }
    }

    /**
     * Adds param with non-string value (e.g. Map, List, Set).
     *
     * @param key   the key name for the new param.
     * @param value the non-string value object for the new param.
     */
    public void put(String key, Map value) {
        if (key != null && value != null) {
            urlParamsWithObjects.put(key, value);
        }
    }

    public void put(String key, List value) {
        if (key != null && value != null) {
            urlParamsWithObjects.put(key, value);
        }
    }

    public void put(String key, Object value) {
        if (key != null && value != null) {
            urlParamsWithObjects.put(key, value);
        }
    }

    /**
     * Adds a int value to the request.
     *
     * @param key   the key name for the new param.
     * @param value the value int for the new param.
     */
    public void put(String key, int value) {
        if (key != null) {
            urlParams.put(key, String.valueOf(value));
        }
    }

    /**
     * Adds a long value to the request.
     *
     * @param key   the key name for the new param.
     * @param value the value long for the new param.
     */
    public void put(String key, long value) {
        if (key != null) {
            urlParams.put(key, String.valueOf(value));
        }
    }

    /**
     * Adds string value to param which can have more than one value.
     *
     * @param key   the key name for the param, either existing or new.
     * @param value the value string for the new param.
     */
    public void add(String key, String value) {
        if (key != null && value != null) {
            Object params = urlParamsWithObjects.get(key);
            if (params == null) {
                // Backward compatible, which will result in "k=v1&k=v2&k=v3"
                params = new HashSet<String>();
                this.put(key, params);
            }
            if (params instanceof List) {
                ((List) params).add(value);
            } else if (params instanceof Set) {
                ((Set) params).add(value);
            }
        }
    }

    /**
     * Removes a parameter from the request.
     *
     * @param key the key name for the parameter to remove.
     */
    public void remove(String key) {
        urlParams.remove(key);
        streamParams.remove(key);
        fileParams.remove(key);
        urlParamsWithObjects.remove(key);
    }

    /**
     * Check if a parameter is defined.
     *
     * @param key the key name for the parameter to check existence.
     * @return Boolean
     */
    public boolean has(String key) {
        return urlParams.get(key) != null || streamParams.get(key) != null
                || fileParams.get(key) != null || urlParamsWithObjects.get(key) != null;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
            if (result.length() > 0) result.append("&");

            result.append(entry.getKey());
            result.append("=");
            result.append(entry.getValue());
        }

        for (ConcurrentHashMap.Entry<String, StreamWrapper> entry : streamParams.entrySet()) {
            if (result.length() > 0) result.append("&");

            result.append(entry.getKey());
            result.append("=");
            result.append("STREAM");
        }

        for (ConcurrentHashMap.Entry<String, FileWrapper> entry : fileParams.entrySet()) {
            if (result.length() > 0) result.append("&");

            result.append(entry.getKey());
            result.append("=");
            result.append("FILE");
        }

        List<BasicNameValuePair> params = getParamsList(null, urlParamsWithObjects);
        for (BasicNameValuePair kv : params) {
            if (result.length() > 0) result.append("&");

            result.append(kv.getName());
            result.append("=");
            result.append(kv.getValue());
        }

        return result.toString();
    }

    public void setHttpEntityIsRepeatable(boolean isRepeatable) {
        this.isRepeatable = isRepeatable;
    }

    public void setUseJsonStreamer(boolean useJsonStreamer) {
        this.useJsonStreamer = useJsonStreamer;
    }

    /**
     * Set global flag which determines whether to automatically close input streams on successful
     * upload.
     *
     * @param flag boolean whether to automatically close input streams
     */
    public void setAutoCloseInputStreams(boolean flag) {
        autoCloseInputStreams = flag;
    }

    private HttpEntity createFormEntity() {
        try {
            return new UrlEncodedFormEntity(getParamsList(), contentEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "createFormEntity failed", e);
            return null; // Can happen, if the 'contentEncoding' won't be HTTP.UTF_8
        }
    }


    protected List<BasicNameValuePair> getParamsList() {
        List<BasicNameValuePair> lparams = new LinkedList();

        for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
            lparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        lparams.addAll(getParamsList(null, urlParamsWithObjects));

        return lparams;
    }

    private List<BasicNameValuePair> getParamsList(String key, Object value) {
        List<BasicNameValuePair> params = new LinkedList();
        if (value instanceof Map) {
            Map map = (Map) value;
            List list = new ArrayList<Object>(map.keySet());
            // Ensure consistent ordering in query string
            Collections.sort(list);
            for (Object nestedKey : list) {
                if (nestedKey instanceof String) {
                    Object nestedValue = map.get(nestedKey);
                    if (nestedValue != null) {
                        params.addAll(getParamsList(
                                key == null ? (String) nestedKey : String.format("%s[%s]", key, nestedKey),
                                nestedValue));
                    }
                }
            }
        } else if (value instanceof List) {
            List list = (List) value;
            int listSize = list.size();
            for (int nestedValueIndex = 0; nestedValueIndex < listSize; nestedValueIndex++) {
                params.addAll(getParamsList(String.format("%s[%d]", key, nestedValueIndex),
                        list.get(nestedValueIndex)));
            }
        } else if (value instanceof Object[]) {
            Object[] array = (Object[]) value;
            int arrayLength = array.length;
            for (int nestedValueIndex = 0; nestedValueIndex < arrayLength; nestedValueIndex++) {
                params.addAll(getParamsList(String.format("%s[%d]", key, nestedValueIndex),
                        array[nestedValueIndex]));
            }
        } else if (value instanceof Set) {
            Set set = (Set) value;
            for (Object nestedValue : set) {
                params.addAll(getParamsList(key, nestedValue));
            }
        } else {
            params.add(new BasicNameValuePair(key, value.toString()));
        }
        return params;
    }

    public String getParamString() {
        return URLEncodedUtils.format(getParamsList(), contentEncoding).replaceAll("\\+", "%20");
    }

    public String getBody() {
        JSONObject json = new JSONObject();
        for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
            try {
                json.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        for (ConcurrentHashMap.Entry<String, Object> entry : urlParamsWithObjects.entrySet()) {
            try {
                Object value = entry.getValue();
                String key = entry.getKey();
                if (value instanceof List) {
                    JSONArray jsonArray = new JSONArray();
                    List list = (List) value;
                    for (Object item : list) {
                        jsonArray.put(item);
                    }
                    json.put(key, jsonArray);
                } else if (value instanceof Map) {
                    JSONObject jsonObj = new JSONObject((Map) value);
                    json.put(key, jsonObj);
                } else {
                    json.put(key, value);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return json.toString();
    }

    public static class FileWrapper {
        public final File file;
        public final String contentType;

        public FileWrapper(File file, String contentType) {
            this.file = file;
            this.contentType = contentType;
        }
    }

    public static class StreamWrapper {
        public final InputStream inputStream;
        public final String name;
        public final String contentType;
        public final boolean autoClose;

        public StreamWrapper(InputStream inputStream, String name, String contentType, boolean autoClose) {
            this.inputStream = inputStream;
            this.name = name;
            this.contentType = contentType;
            this.autoClose = autoClose;
        }

        static StreamWrapper newInstance(InputStream inputStream, String name, String contentType,
                                         boolean autoClose) {
            return new StreamWrapper(inputStream, name, contentType == null
                    ? APPLICATION_OCTET_STREAM
                    : contentType, autoClose);
        }
    }
}


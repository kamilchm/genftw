/*
 * Copyright 2011 GenFTW contributors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package genftw.core.match;

import genftw.api.MetaData;
import genftw.api.Where;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;

/**
 * Matches elements against given {@linkplain Where#metaData() meta-data match string}.
 */
public class MetaDataMatcher {

    private static final Pattern META_DATA_PROPERTY_PATTERN = Pattern.compile("\\[([^\\[\\]]*)\\]");

    private final Elements elementUtils;

    public MetaDataMatcher(Elements elementUtils) {
        this.elementUtils = elementUtils;
    }

    public boolean matches(Element elm, String metaDataMatchString) {
        // Validate match string
        if (Where.DONT_MATCH.equals(metaDataMatchString)) {
            return true;
        }

        // Find meta-data annotation
        MetaDataMirror metaDataMirror = getMetaDataMirror(elm);
        if (metaDataMirror == null) {
            return false;
        }

        // Match by kind
        int propertyStartIndex = metaDataMatchString.indexOf("[");
        String kind = propertyStartIndex == -1
                ? metaDataMatchString
                : metaDataMatchString.substring(0, propertyStartIndex);

        if (!"*".equals(kind) && !kind.equals(metaDataMirror.kind())) {
            return false;
        }

        // Match by properties
        Map<String, String> propertyMap = getPropertyMap(metaDataMirror.properties());
        Matcher matcher = META_DATA_PROPERTY_PATTERN.matcher(metaDataMatchString);

        while (matcher.find()) {
            String propertyExpression = matcher.group(1);
            int eqStartIndex = propertyExpression.indexOf("=");
            String propertyName, propertyValue = null;

            if (eqStartIndex == -1) {
                propertyName = propertyExpression;
            } else {
                propertyName = propertyExpression.substring(0, eqStartIndex);
                propertyValue = propertyExpression.substring(eqStartIndex + 1);
            }

            // Match by property name
            if (propertyName.isEmpty() || !propertyMap.containsKey(propertyName)) {
                return false;
            }

            // Match by property value
            if (propertyValue != null && !propertyValue.equals(propertyMap.get(propertyName))) {
                return false;
            }
        }

        return true;
    }

    Map<String, String> getPropertyMap(String[] metaDataProperties) {
        Map<String, String> propertyMap = new HashMap<String, String>(metaDataProperties.length);

        for (String propertyExpression : metaDataProperties) {
            int eqStartIndex = propertyExpression.indexOf("=");
            String propertyName, propertyValue = null;

            if (eqStartIndex == -1) {
                propertyName = propertyExpression;
            } else {
                propertyName = propertyExpression.substring(0, eqStartIndex);
                propertyValue = propertyExpression.substring(eqStartIndex + 1);
            }

            propertyMap.put(propertyName, propertyValue);
        }

        return propertyMap;
    }

    AnnotationMirror findMetaDataAnnotation(Element elm) {
        for (AnnotationMirror m : elementUtils.getAllAnnotationMirrors(elm)) {
            DeclaredType annotationType = m.getAnnotationType();
            String annotationTypeName = annotationType.toString();

            if (MetaData.class.getName().equals(annotationTypeName)) {
                return m;
            } else if (annotationTypeName.startsWith("java.lang.annotation")) {
                // Skip annotations from java.lang.annotation package to avoid infinite recursion
                continue;
            }

            // Recursively scan annotations of this annotation type
            AnnotationMirror found = findMetaDataAnnotation(annotationType.asElement());

            if (found != null) {
                return found;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    MetaDataMirror getMetaDataMirror(Element elm) {
        AnnotationMirror metaDataAnnotation = findMetaDataAnnotation(elm);
        MetaDataMirror result = null;

        if (metaDataAnnotation != null) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> metaDataValues =
                    elementUtils.getElementValuesWithDefaults(metaDataAnnotation);

            String kind = null;
            String[] properties = null;

            // Parse meta-data kind and properties from annotation values
            for (ExecutableElement key : metaDataValues.keySet()) {
                if ("kind".equals(key.getSimpleName().toString())) {
                    kind = (String) metaDataValues.get(key).getValue();
                } else if ("properties".equals(key.getSimpleName().toString())) {
                    properties = getMetaDataProperties((List<AnnotationValue>) metaDataValues.get(key).getValue());
                }
            }

            result = new MetaDataMirror(kind, properties);
        }

        return result;
    }

    String[] getMetaDataProperties(List<AnnotationValue> propertyList) {
        String[] properties = new String[propertyList.size()];
        int i = 0;

        for (AnnotationValue v : propertyList) {
            properties[i++] = (String) v.getValue();
        }

        return properties;
    }

}

class MetaDataMirror {

    private final String kind;
    private final String[] properties;

    public MetaDataMirror(String kind, String[] properties) {
        this.kind = kind;
        this.properties = properties;
    }

    public String kind() {
        return kind;
    }

    public String[] properties() {
        return properties;
    }

}

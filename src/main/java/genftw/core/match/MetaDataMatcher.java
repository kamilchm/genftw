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
import java.util.LinkedList;
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

    private static final String ANY_KIND = "*";
    private static final String PROPERTY_VALUE_SEPARATOR = "=";
    private static final String META_DATA_ANNOTATION_TARGET_PROPERTY_PREFIX = "@";

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
        String matchedKind = propertyStartIndex == -1
                ? metaDataMatchString
                : metaDataMatchString.substring(0, propertyStartIndex);

        if (!ANY_KIND.equals(matchedKind) && !matchedKind.equals(metaDataMirror.kind())) {
            return false;
        }

        // Match by properties
        Map<String, String> propertyMap = metaDataMirror.propertyMap();
        Map<String, String> annotationTargetPropertyMap = metaDataMirror.annotationTargetPropertyMap();
        Matcher matcher = META_DATA_PROPERTY_PATTERN.matcher(metaDataMatchString);

        while (matcher.find()) {
            MetaDataProperty matchedProperty = getProperty(matcher.group(1));
            String matchedPropertyName = matchedProperty.name();
            String matchedPropertyValue = matchedProperty.value();
            Map<String, String> propertyMapToCheck = propertyMap;

            if (matchedPropertyName.startsWith(META_DATA_ANNOTATION_TARGET_PROPERTY_PREFIX)) {
                matchedPropertyName = matchedPropertyName.substring(
                        META_DATA_ANNOTATION_TARGET_PROPERTY_PREFIX.length());
                propertyMapToCheck = annotationTargetPropertyMap;
            }

            if (!propertyMatches(matchedPropertyName, matchedPropertyValue, propertyMapToCheck)) {
                return false;
            }
        }

        return true;
    }

    boolean propertyMatches(String propertyName, String propertyValue, Map<String, String> propertyMap) {
        boolean result = true;

        // Match by name
        if (result) {
            result = result && !propertyName.isEmpty() && propertyMap.containsKey(propertyName);
        }

        // Match by value
        if (result && propertyValue != null) {
            result = result && propertyValue.equals(propertyMap.get(propertyName));
        }

        return result;
    }

    MetaDataProperty getProperty(String propertyExpression) {
        int eqStartIndex = propertyExpression.indexOf(PROPERTY_VALUE_SEPARATOR);
        String propertyName, propertyValue = null;

        if (eqStartIndex == -1) {
            propertyName = propertyExpression;
        } else {
            propertyName = propertyExpression.substring(0, eqStartIndex);
            propertyValue = propertyExpression.substring(eqStartIndex + 1);
        }

        return new MetaDataProperty(propertyName, propertyValue);
    }

    Map<String, String> getPropertyMap(MetaDataProperty[] properties) {
        Map<String, String> propertyMap = new HashMap<String, String>(properties.length);

        for (MetaDataProperty p : properties) {
            propertyMap.put(p.name(), p.value());
        }

        return propertyMap;
    }

    NestedAnnotationMirror findMetaDataAnnotation(Element elm) {
        for (AnnotationMirror m : elementUtils.getAllAnnotationMirrors(elm)) {
            DeclaredType annotationType = m.getAnnotationType();
            String annotationTypeName = annotationType.toString();

            if (MetaData.class.getName().equals(annotationTypeName)) {
                // Return the first meta-data annotation found
                return new NestedAnnotationMirror(m, null);
            } else if (annotationTypeName.startsWith("java.lang.annotation")) {
                // Skip annotations from java.lang.annotation package to avoid infinite recursion
                continue;
            }

            // Recursively scan annotations of this annotation type
            NestedAnnotationMirror found = findMetaDataAnnotation(annotationType.asElement());

            if (found != null) {
                return new NestedAnnotationMirror(found.annotation(), m);
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    MetaDataMirror getMetaDataMirror(Element elm) {
        NestedAnnotationMirror metaDataAnnotation = findMetaDataAnnotation(elm);
        MetaDataMirror result = null;

        if (metaDataAnnotation != null) {
            String kind = null;
            Map<String, String> propertyMap = null, annotationTargetPropertyMap = null;

            // Parse meta-data kind and properties from annotation values
            Map<? extends ExecutableElement, ? extends AnnotationValue> metaDataAnnotationValues =
                    elementUtils.getElementValuesWithDefaults(metaDataAnnotation.annotation());

            for (ExecutableElement key : metaDataAnnotationValues.keySet()) {
                String annotationElementName = key.getSimpleName().toString();

                if ("kind".equals(annotationElementName)) {
                    kind = (String) metaDataAnnotationValues.get(key).getValue();
                } else if ("properties".equals(annotationElementName)) {
                    propertyMap = getMetaDataPropertyMap(
                            (List<AnnotationValue>) metaDataAnnotationValues.get(key).getValue());
                }
            }

            // Parse all String values from the annotation target
            AnnotationMirror metaDataAnnotationTarget = metaDataAnnotation.annotationTarget();
            List<MetaDataProperty> annotationTargetProperties = new LinkedList<MetaDataProperty>();

            if (metaDataAnnotationTarget != null) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> metaDataAnnotationTargetValues =
                        elementUtils.getElementValuesWithDefaults(metaDataAnnotationTarget);

                for (ExecutableElement key : metaDataAnnotationTargetValues.keySet()) {
                    Object value = metaDataAnnotationTargetValues.get(key).getValue();

                    if (String.class.isAssignableFrom(value.getClass())) {
                        String annotationElementName = key.getSimpleName().toString();
                        annotationTargetProperties.add(
                                new MetaDataProperty(annotationElementName, (String) value));
                    }
                }

                annotationTargetPropertyMap = getPropertyMap(
                        annotationTargetProperties.toArray(new MetaDataProperty[0]));
            }

            result = new MetaDataMirror(kind, propertyMap, annotationTargetPropertyMap);
        }

        return result;
    }

    Map<String, String> getMetaDataPropertyMap(List<AnnotationValue> propertyList) {
        MetaDataProperty[] properties = new MetaDataProperty[propertyList.size()];
        int i = 0;

        for (AnnotationValue v : propertyList) {
            properties[i++] = getProperty((String) v.getValue());
        }

        return getPropertyMap(properties);
    }

}

class NestedAnnotationMirror {

    private final AnnotationMirror annotation;
    private final AnnotationMirror annotationTarget;

    NestedAnnotationMirror(AnnotationMirror annotation, AnnotationMirror annotationTarget) {
        this.annotation = annotation;
        this.annotationTarget = annotationTarget;
    }

    public AnnotationMirror annotation() {
        return annotation;
    }

    public AnnotationMirror annotationTarget() {
        return annotationTarget;
    }

}

class MetaDataProperty {

    private final String name;
    private final String value;

    MetaDataProperty(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

}

class MetaDataMirror {

    private final String kind;
    private final Map<String, String> propertyMap;
    private final Map<String, String> annotationTargetPropertyMap;

    MetaDataMirror(String kind, Map<String, String> propertyMap, Map<String, String> annotationTargetPropertyMap) {
        this.kind = kind;
        this.propertyMap = propertyMap;
        this.annotationTargetPropertyMap = annotationTargetPropertyMap;
    }

    public String kind() {
        return kind;
    }

    public Map<String, String> propertyMap() {
        return propertyMap;
    }

    public Map<String, String> annotationTargetPropertyMap() {
        return annotationTargetPropertyMap;
    }

}

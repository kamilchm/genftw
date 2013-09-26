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

package genftw.core.util;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Contains utility methods for working with source elements, complementing standard {@linkplain Elements element
 * utilities}.
 * <p/>
 * Intended for use within templates.
 */
public class ElementGoodies {

  private final Elements elementUtils;

  public ElementGoodies(Elements elementUtils) {
    this.elementUtils = elementUtils;
  }

  public String getPackageOf(Element elm) {
    return elementUtils.getPackageOf(elm).getQualifiedName().toString();
  }

  public boolean hasAnnotation(Element elm, String annotationName) {
    return getAnnotationByName(elm, annotationName) != null;
  }

  public Object getAnnotationValue(Element elm, String annotationName) {
    AnnotationMirror annotation = getAnnotationByName(elm, annotationName);
    if (annotation != null) {
      for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotation.getElementValues()
          .entrySet()) {
        if (entry.getKey().getSimpleName().contentEquals(annotationName)) return entry.getValue();
      }
    }
    return null;
  }

  Set<String> getAllAnnotationNames(Element elm) {
    Set<String> annotationNames = new HashSet<String>();
    for (AnnotationMirror a : elementUtils.getAllAnnotationMirrors(elm)) {
      annotationNames.add(a.getAnnotationType().toString());
    }
    return annotationNames;
  }

  private AnnotationMirror getAnnotationByName(Element elm, String name) {
    for (AnnotationMirror annotation : elementUtils.getAllAnnotationMirrors(elm)) {
      if (annotation.getAnnotationType().toString().equals(name)) return annotation;
    }
    return null;
  }
}

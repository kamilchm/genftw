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

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;

/**
 * Contains utility methods for working with source elements.
 * <p>
 * Intended for use within templates.
 */
public class ElementGoodies {

    private final Elements elementUtils;

    public ElementGoodies(Elements elementUtils) {
        this.elementUtils = elementUtils;
    }

    public Elements getElementUtils() {
        return elementUtils;
    }

    public String getPackageOf(Element elm) {
        return elementUtils.getPackageOf(elm).getQualifiedName().toString();
    }

    public boolean hasAnnotation(Element elm, String annotationName) {
        return getAllAnnotationNames(elm).contains(annotationName);
    }

    Set<String> getAllAnnotationNames(Element elm) {
        Set<String> annotationNames = new HashSet<String>();
        for (AnnotationMirror a : elementUtils.getAllAnnotationMirrors(elm)) {
            annotationNames.add(a.getAnnotationType().toString());
        }
        return annotationNames;
    }

}

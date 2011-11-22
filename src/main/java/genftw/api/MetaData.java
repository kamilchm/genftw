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

package genftw.api;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Adds custom meta-data to arbitrary source elements.
 * <p>
 * Annotated elements can be matched by generator methods using {@linkplain Where#metaData() meta-data match string}.
 * <p>
 * When matching elements, this annotation may appear directly on an element, or be nested as a meta-annotation present
 * on some other element annotation.
 * 
 * @see Where
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface MetaData {

    /**
     * An optional kind that defines stereotype of the annotated element.
     */
    String kind() default "";

    /**
     * Meta-data properties, each in {@code name} or {@code name=value} format.
     */
    String[] properties() default {};

}

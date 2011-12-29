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

package genftw.core;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import freemarker.template.TemplateException;
import genftw.api.ForAllElements;
import genftw.api.ForEachElement;
import genftw.api.Produces;
import genftw.api.Where;
import genftw.core.match.ElementFinder;

import java.io.IOException;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;
import javax.tools.StandardLocation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GeneratorMethodTest {

    @Mock
    ExecutableElement methodElement;

    @Mock
    Elements elementUtils;

    @Mock
    ElementFinder elementFinder;

    @Mock
    ProcessorLogger logger;

    @Mock
    ForAllElements groupMatchAnnotation;

    @Mock
    ForEachElement loopMatchAnnotation;

    @Mock
    Produces outputAnnotation;

    @Mock
    GeneratorMethodTemplate methodTemplate;

    @InjectMocks
    GeneratorMethod tested;

    @Test
    public void processSimpleMethod_expectedBehavior() throws IOException, TemplateException {
        stubOutputAnnotation(StandardLocation.SOURCE_OUTPUT, "com/test/package/OutputFile");

        tested.processSimpleMethod(methodTemplate);

        verify(methodTemplate, never()).setRootModelMapping(anyString(), anyObject());
        verify(methodTemplate).process(StandardLocation.SOURCE_OUTPUT, "com/test/package/OutputFile");
    }

    @Test
    public void processGroupMatchMethod_expectedBehavior() throws IOException, TemplateException {
        stubGroupMatchAnnotation("one", "two");
        stubOutputAnnotation(StandardLocation.SOURCE_OUTPUT, "com/test/package/OutputFile");

        Element[] matchedElementsForMetaDataOne = new Element[] { mock(Element.class) };
        stubElementFinderWithMatchDefinition(groupMatchAnnotation.value()[0],
                matchedElementsForMetaDataOne, "matchResultOne");

        Element[] matchedElementsForMetaDataTwo = new Element[] {};
        stubElementFinderWithMatchDefinition(groupMatchAnnotation.value()[1],
                matchedElementsForMetaDataTwo, "matchResultTwo");

        tested.processGroupMatchMethod(methodTemplate);

        verify(methodTemplate).setRootModelMapping("matchResultOne", matchedElementsForMetaDataOne);
        verify(methodTemplate).setRootModelMapping("matchResultTwo", matchedElementsForMetaDataTwo);
        verify(methodTemplate).process(StandardLocation.SOURCE_OUTPUT, "com/test/package/OutputFile");
    }

    @Test
    public void processGroupMatchMethod_noMatchDefinitions() throws IOException, TemplateException {
        stubGroupMatchAnnotation();
        stubOutputAnnotation(StandardLocation.SOURCE_OUTPUT, "com/test/package/OutputFile");

        tested.processGroupMatchMethod(methodTemplate);

        verify(methodTemplate, never()).setRootModelMapping(anyString(), anyObject());
        verify(methodTemplate).process(StandardLocation.SOURCE_OUTPUT, "com/test/package/OutputFile");
    }

    @Test
    public void processLoopMatchMethod_expectedBehavior() throws IOException, TemplateException {
        stubLoopMatchAnnotation("each", "extraOne", "extraTwo");
        stubOutputAnnotation(StandardLocation.SOURCE_OUTPUT, "com/test/package/OutputFile");

        Element loopElementOne = mockElement("MyClassOne", "com.test.package");
        Element loopElementTwo = mockElement("MyClassTwo", "com.test.package");
        Element[] matchedElementsForMetaDataEach = new Element[] { loopElementOne, loopElementTwo };
        stubElementFinderWithMatchDefinition(loopMatchAnnotation.value(),
                matchedElementsForMetaDataEach, "matchResultEach");

        Element[] matchedElementsForMetaDataExtraOne = new Element[] { mock(Element.class) };
        stubElementFinderWithMatchDefinition(loopMatchAnnotation.matchExtraElements()[0],
                matchedElementsForMetaDataExtraOne, "matchResultExtraOne");

        Element[] matchedElementsForMetaDataExtraTwo = new Element[] {};
        stubElementFinderWithMatchDefinition(loopMatchAnnotation.matchExtraElements()[1],
                matchedElementsForMetaDataExtraTwo, "matchResultExtraTwo");

        tested.processLoopMatchMethod(methodTemplate);

        verify(methodTemplate).setRootModelMapping("matchResultExtraOne",
                matchedElementsForMetaDataExtraOne);
        verify(methodTemplate).setRootModelMapping("matchResultExtraTwo",
                matchedElementsForMetaDataExtraTwo);
        verify(methodTemplate).setRootModelMapping("matchResultEach", loopElementOne);
        verify(methodTemplate).setRootModelMapping("matchResultEach", loopElementTwo);
        verify(methodTemplate, times(2)).process(StandardLocation.SOURCE_OUTPUT,
                "com/test/package/OutputFile");
    }

    @Test
    public void processLoopMatchMethod_noMatchDefinitions() throws IOException, TemplateException {
        stubLoopMatchAnnotation("each");
        stubOutputAnnotation(StandardLocation.SOURCE_OUTPUT, "com/test/package/OutputFile");

        stubElementFinderWithMatchDefinition(loopMatchAnnotation.value(),
                new Element[] {}, "matchResultEach");

        tested.processLoopMatchMethod(methodTemplate);

        verify(methodTemplate, never()).setRootModelMapping(anyString(), anyObject());
        verify(methodTemplate, never()).process(any(StandardLocation.class), anyString());
        verify(logger).warning(anyString(), any(Element.class));
    }

    void stubOutputAnnotation(StandardLocation outputRootLocation, String outputFile) {
        when(outputAnnotation.outputRootLocation()).thenReturn(outputRootLocation);
        when(outputAnnotation.output()).thenReturn(outputFile);
        when(methodElement.getAnnotation(Produces.class)).thenReturn(outputAnnotation);
    }

    void stubElementFinderWithMatchDefinition(Where def,
            Element[] matchedElements, String matchResultVariable) {
        when(elementFinder.getElementsFound(def)).thenReturn(matchedElements);
        when(def.matchResultVariable()).thenReturn(matchResultVariable);
    }

    @Test
    public void resolveOutputFile_withSupportedVariables() {
        Element element = mockElement("MyClass", "com.test.package");

        String result = tested.resolveOutputFile(element,
                "root/{packageElementPath}/{elementSimpleName}Generated");

        assertThat(result, equalTo("root/com/test/package/MyClassGenerated"));
    }

    @Test
    public void resolveOutputFile_withUnknownVariables() {
        Element element = mockElement("MyClass", "com.test.package");

        String result = tested.resolveOutputFile(element,
                "root/{packageElementPath}/{unknownVariable}Generated");

        assertThat(result, equalTo("root/com/test/package/{unknownVariable}Generated"));
    }

    Element mockElement(String simpleName, String packageQualifiedName) {
        Element element = mock(Element.class);
        PackageElement packageElement = mock(PackageElement.class);

        Name elementSimpleName = when(mock(Name.class).toString())
                .thenReturn(simpleName).getMock();
        Name packageElementQualifiedName = when(mock(Name.class).toString())
                .thenReturn(packageQualifiedName).getMock();

        when(elementUtils.getPackageOf(element)).thenReturn(packageElement);
        when(element.getSimpleName()).thenReturn(elementSimpleName);
        when(packageElement.getQualifiedName()).thenReturn(packageElementQualifiedName);

        return element;
    }

    @Test
    public void getMatchDefinitions_groupMatchAnnotation() {
        stubGroupMatchAnnotation("one", "two");

        Where[] result = tested.getMatchDefinitions();

        assertThat(result.length, equalTo(2));
        assertThat(result[0].metaData(), equalTo("one"));
        assertThat(result[1].metaData(), equalTo("two"));
    }

    @Test
    public void getMatchDefinitions_loopMatchAnnotation() {
        stubLoopMatchAnnotation("each", "extraOne", "extraTwo");

        Where[] result = tested.getMatchDefinitions();

        assertThat(result.length, equalTo(3));
        assertThat(result[0].metaData(), equalTo("each"));
        assertThat(result[1].metaData(), equalTo("extraOne"));
        assertThat(result[2].metaData(), equalTo("extraTwo"));
    }

    @Test
    public void getMatchDefinitions_withoutMatchAnnotation() {
        Where[] result = tested.getMatchDefinitions();

        assertThat(result.length, equalTo(0));
    }

    void stubGroupMatchAnnotation(String... metaData) {
        Where[] defs = new Where[metaData.length];

        for (int i = 0; i < metaData.length; i++) {
            defs[i] = when(mock(Where.class).metaData()).thenReturn(metaData[i]).getMock();
        }

        when(groupMatchAnnotation.value()).thenReturn(defs);
        when(methodElement.getAnnotation(ForAllElements.class)).thenReturn(groupMatchAnnotation);
    }

    void stubLoopMatchAnnotation(String valueMetaData, String... extraMetaData) {
        Where def = when(mock(Where.class).metaData()).thenReturn(valueMetaData).getMock();
        Where[] extraDefs = new Where[extraMetaData.length];

        for (int i = 0; i < extraMetaData.length; i++) {
            extraDefs[i] = when(mock(Where.class).metaData()).thenReturn(extraMetaData[i]).getMock();
        }

        when(loopMatchAnnotation.value()).thenReturn(def);
        when(loopMatchAnnotation.matchExtraElements()).thenReturn(extraDefs);
        when(methodElement.getAnnotation(ForEachElement.class)).thenReturn(loopMatchAnnotation);
    }

}

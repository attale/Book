/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.java.archives.internal

import org.apache.tools.ant.taskdefs.Manifest
import org.apache.tools.ant.taskdefs.Manifest.Attribute
import org.apache.tools.ant.taskdefs.Manifest.Section
import org.gradle.api.internal.file.FileResolver
import org.gradle.test.fixtures.file.TestFile
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll


class DefaultManifestTest extends Specification {
    def static final MANIFEST_VERSION_MAP = ['Manifest-Version': '1.0']
    def fileResolver = Mock(FileResolver)
    DefaultManifest gradleManifest = new DefaultManifest(fileResolver)

    @Rule
    public final TestNameTestDirectoryProvider tmpDir = new TestNameTestDirectoryProvider()

    def testInitWithFileReolver() {
        expect:
        gradleManifest.attributes == MANIFEST_VERSION_MAP
        !gradleManifest.sections
        !gradleManifest.mergeSpecs
    }

    def testInitWithPathFileResolver() {
        TestFile manifestFile = tmpDir.file('somefile')
        def fileMap = [Key2: 'value2File', key4: 'value4File', key6: 'value6File']
        def fileSectionMap = [Keysec2: 'value2Secfile', keysec4: 'value5Secfile', keysec6: 'value6Secfile']
        String content = ''
        fileMap.each {key, value ->
            content += String.format("%s: %s%n", key, value)
        }
        content += String.format("Name: %s%n", 'section')
        fileSectionMap.each {key, value ->
            content += String.format("%s: %s%n", key, value)
        }
        manifestFile.write(content)
        fileResolver.resolve('file') >> manifestFile

        when:
        DefaultManifest manifest = new DefaultManifest('file', fileResolver)

        then:
        manifest.getAttributes() == fileMap + MANIFEST_VERSION_MAP
        manifest.sections.section == fileSectionMap
    }

    def testAddMainAttributes() {
        Map attributes = [key1: 'value1', key2: 'value2']
        Map attributes2 = [key3: 'value3']

        when:
        def expectToReturnSelf = gradleManifest.mainAttributes(attributes)
        gradleManifest.attributes(attributes2)

        then:
        gradleManifest.is(expectToReturnSelf)
        gradleManifest.getAttributes() == attributes + attributes2 + MANIFEST_VERSION_MAP
    }

    def testAddSectionAttributes() {
        String section1 = 'section1'
        String section2 = 'section2'
        Map attributes = [key1: 'value1', key2: 'value2']
        Map attributes2 = [key3: 'value3']
        Map attributes3 = [key3: 'value3']

        when:
        def expectToReturnSelf = gradleManifest.attributes(attributes, section1)
        gradleManifest.attributes(attributes2, section1)
        gradleManifest.attributes(attributes3, section2)

        then:
        gradleManifest.is(expectToReturnSelf)
        gradleManifest.sections.section1 == attributes + attributes2
        gradleManifest.sections.section2 == attributes3
        gradleManifest.getSections() == [section1: attributes + attributes2, section2: attributes3]
    }

    def clear() {
        gradleManifest.attributes(key1: 'value1')
        gradleManifest.attributes('section', key1: 'value1')
        gradleManifest.from(new DefaultManifest(Mock(FileResolver)))

        when:
        gradleManifest.clear()

        then:
        gradleManifest.getAttributes() == MANIFEST_VERSION_MAP
        !gradleManifest.sections
        !gradleManifest.mergeSpecs
    }

    def merge() {
        gradleManifest.attributes(key1: 'value1')
        gradleManifest.from(new DefaultManifest(fileResolver).attributes(key2: 'value2', key3: 'value3')) {
            eachEntry { details ->
                if (details.key == 'key3') {
                    details.exclude()
                }
            }
        }
        gradleManifest.from(new DefaultManifest(fileResolver).attributes(key4: 'value4'))

        expect:
        gradleManifest.effectiveManifest.getAttributes() == [key1: 'value1', key2: 'value2', key4: 'value4'] + MANIFEST_VERSION_MAP
    }

    def write() {
        Map testMainAttributes = [key1: 'value1', key2: 'value2', key3: 'value3', key4: 'value4'] as LinkedHashMap
        Map testSectionAttributes = [sectionkey1: 'sectionvalue1']
        String testSection = 'section'

        DefaultManifest gradleManifest = new DefaultManifest(fileResolver)
        gradleManifest.from(new DefaultManifest(fileResolver).attributes(testMainAttributes))
        gradleManifest.attributes(testSectionAttributes, testSection)

        Manifest expectedManifest = new Manifest()
        expectedManifest.addConfiguredAttribute(new Attribute('key1', 'value1'))
        expectedManifest.addConfiguredAttribute(new Attribute('key2', 'value2'))
        expectedManifest.addConfiguredAttribute(new Attribute('key3', 'value3'))
        expectedManifest.addConfiguredAttribute(new Attribute('key4', 'value4'))
        expectedManifest.addConfiguredAttribute(new Attribute('Manifest-Version', '1.0'))
        Section section = new Section()
        section.setName testSection
        section.addConfiguredAttribute(new Attribute('sectionkey1', 'sectionvalue1'))
        expectedManifest.addConfiguredSection(section)
        def StringWriter stringWriter = new StringWriter()

        when:
        gradleManifest.writeTo(stringWriter)
        Manifest actualManifest = new Manifest(new StringReader(stringWriter.toString()))
        def actualKeys = [] as Set
        actualManifest.getMainSection().getAttributeKeys().each { element ->
            actualKeys.add(element)
        }

        then:
        actualManifest == expectedManifest
        actualKeys == testMainAttributes.keySet()
    }

    def writeWithPath() {
        TestFile manifestFile = tmpDir.file('someNonexistingDir').file('someFile')
        DefaultManifest manifest = new DefaultManifest(fileResolver).attributes(key1: 'value1')
        fileResolver.resolve('file') >> manifestFile

        when:
        manifest.writeTo('file')
        Manifest fileManifest = manifestFile.withReader { new Manifest(it) }
        Manifest expectedManifest = new Manifest()
        expectedManifest.addConfiguredAttribute(new Attribute('key1', 'value1'))
        expectedManifest.addConfiguredAttribute(new Attribute('Manifest-Version', '1.0'))

        then:
        fileManifest.equals(expectedManifest)
    }

    @Unroll
    def "can read manifest section starting with #nameAttribute"() {
        given:
        TestFile manifestFile = tmpDir.file('someManifestFile')
        fileResolver.resolve('file') >> manifestFile

        and:
        manifestFile.text = """
            Manifest-Version: 1.0
            Some-Main-Attribute: someValue
            $nameAttribute: someSection
            Some-Section-Attribute: some other value
        """.stripIndent().trim() + '\n'

        when:
        DefaultManifest manifest = new DefaultManifest('file', fileResolver);

        then:
        manifest.getAttributes().get('Some-Main-Attribute') == 'someValue'
        manifest.getSections().get('someSection').get('Some-Section-Attribute') == 'some other value'

        where:
        nameAttribute | _
        'NAME'        | _
        'name'        | _
        'Name'        | _
        'nAme'        | _
        'naMe'        | _
        'namE'        | _
        'NamE'        | _
    }

    def "demonstrate Java vs. Ant Manifest classes behavior wrt. blank lines"() {
        given:
        TestFile noBlankLinesManifestFile = tmpDir.file('noBlankLinesManifestFile')
        TestFile blankLinesManifestFile = tmpDir.file('blankLinesManifestFile')
        noBlankLinesManifestFile.text = '''
            Manifest-Version: 1.0
            Some-Main-Attribute: someValue
            Name: someSection
            Some-Section-Attribute: some other value
        '''.stripIndent().trim() + '\n'
        blankLinesManifestFile.text = '''
            Manifest-Version: 1.0
            Some-Main-Attribute: someValue

            Name: someSection
            Some-Section-Attribute: some other value
        '''.stripIndent().trim() + '\n'

        when:
        def noBlankLinesJavaManifest = readJavaManifest(noBlankLinesManifestFile)
        def blankLinesJavaManifest = readJavaManifest(blankLinesManifestFile)
        def noBlankLinesAntManifest = readAntManifest(noBlankLinesManifestFile)
        def blankLinesAntManifest = readAntManifest(blankLinesManifestFile)

        then:
        // Java Manifest, no blank lines
        noBlankLinesJavaManifest.mainAttributes.size() == 4
        noBlankLinesJavaManifest.mainAttributes.getValue('Manifest-Version') == '1.0'
        noBlankLinesJavaManifest.mainAttributes.getValue('Some-Main-Attribute') == 'someValue'
        noBlankLinesJavaManifest.mainAttributes.getValue('Name') == 'someSection'
        noBlankLinesJavaManifest.mainAttributes.getValue('Some-Section-Attribute') == 'some other value'
        noBlankLinesJavaManifest.entries.isEmpty()

        and:
        // Java Manifest, blank lines
        blankLinesJavaManifest.mainAttributes.size() == 2
        blankLinesJavaManifest.mainAttributes.getValue('Manifest-Version') == '1.0'
        blankLinesJavaManifest.mainAttributes.getValue('Some-Main-Attribute') == 'someValue'
        blankLinesJavaManifest.entries.size() == 1
        blankLinesJavaManifest.entries.get('someSection').getValue('Some-Section-Attribute') == 'some other value'

        and:
        // Ant Manifest, no blank lines
        Collections.list(noBlankLinesAntManifest.mainSection.attributeKeys).size() == 1
        noBlankLinesAntManifest.mainSection.getAttributeValue('Some-Main-Attribute') == 'someValue'
        Collections.list(noBlankLinesAntManifest.getSectionNames()).size() == 1
        noBlankLinesAntManifest.getSection('someSection').getAttributeValue('Some-Section-Attribute') == 'some other value'

        and:
        // Ant Manifest, blank lines
        Collections.list(blankLinesAntManifest.mainSection.attributeKeys).size() == 1
        blankLinesAntManifest.mainSection.getAttributeValue('Some-Main-Attribute') == 'someValue'
        Collections.list(blankLinesAntManifest.getSectionNames()).size() == 1
        blankLinesAntManifest.getSection('someSection').getAttributeValue('Some-Section-Attribute') == 'some other value'
    }

    def "demonstrate Java vs. Ant Manifest classes behavior wrt. split multi-byte characters"() {
        given:
        TestFile manifestFile = tmpDir.file('someManifestFile')

        and:
        // Means 'long russian text'
        String attributeValue = 'com.acme.example.pack.**, длинный.текст.на.русском.языке.**'
        java.util.jar.Manifest manifest = new java.util.jar.Manifest()
        manifest.mainAttributes.putValue('Manifest-Version', '1.0')
        manifest.mainAttributes.putValue('Another-Looooooong-Name-Entry', attributeValue)
        writeJavaManifest(manifest, manifestFile)

        when:
        def javaManifest = readJavaManifest(manifestFile)
        def antManifest = readAntManifest(manifestFile)

        then:
        javaManifest.getMainAttributes().getValue('Another-Looooooong-Name-Entry') == attributeValue
        antManifest.getMainSection().getAttributeValue('Another-Looooooong-Name-Entry') != attributeValue // Broken!
    }

    def "write with split multi-byte character"() {
        given:
        TestFile manifestFile = tmpDir.file('someManifestFile')
        fileResolver.resolve('manifestFile') >> manifestFile

        and:
        // Means 'long russian text'
        String attributeValue = 'com.acme.example.pack.**, длинный.текст.на.русском.языке.**'
        DefaultManifest gradleManifest = new DefaultManifest(fileResolver)
        gradleManifest.getAttributes().put('Looong-Name-Of-Manifest-Entry', attributeValue)

        when:
        gradleManifest.writeTo('manifestFile')

        then:
        def javaManifest = readJavaManifest(manifestFile)
        javaManifest.mainAttributes.getValue('Looong-Name-Of-Manifest-Entry') == attributeValue
    }

    def "merge with split multi-byte character and sections not preceded by blank lines"() {
        given:
        TestFile manifestFile = tmpDir.file('someManifestFile')
        TestFile mergedFile = tmpDir.file('someMergedManifestFile')
        fileResolver.resolve('manifestFile') >> manifestFile
        fileResolver.resolve('mergedFile') >> mergedFile

        and:
        // Means 'long russian text'
        String attributeValue = 'com.acme.example.pack.**, длинный.текст.на.русском.языке.**'
        java.util.jar.Manifest javaMergedManifest = new java.util.jar.Manifest()
        javaMergedManifest.mainAttributes.putValue('Manifest-Version', '1.0')
        javaMergedManifest.mainAttributes.putValue('Another-Looooooong-Name-Entry', attributeValue)
        def someSection = new java.util.jar.Attributes()
        someSection.putValue('foo', 'bar')
        javaMergedManifest.entries.put("SomeSection", someSection)
        def anotherSection = new java.util.jar.Attributes()
        anotherSection.putValue('bazar', 'cathedral')
        javaMergedManifest.entries.put("AnotherSection", anotherSection)
        mergedFile.withOutputStream { javaMergedManifest.write(it) }

        and:
        mergedFile.bytes = removeBlankLines(mergedFile.bytes)

        and:
        DefaultManifest gradleManifest = new DefaultManifest(fileResolver)
        gradleManifest.getAttributes().put('Looong-Name-Of-Manifest-Entry', attributeValue)
        gradleManifest.from('mergedFile')

        when:
        gradleManifest.writeTo('manifestFile')

        then:
        def javaManifest = readJavaManifest(manifestFile)
        javaManifest.mainAttributes.getValue('Looong-Name-Of-Manifest-Entry') == attributeValue
        javaManifest.mainAttributes.getValue('Another-Looooooong-Name-Entry') == attributeValue
        javaManifest.entries.get('SomeSection').getValue('foo') == 'bar'
        javaManifest.entries.get('AnotherSection').getValue('bazar') == 'cathedral'
    }

    /**
     * Remove blank lines to exercise Ant's Manifest interoperability.
     * Need to work at bytes level to prevent breaking split multi-bytes characters here.
     */
    private static byte[] removeBlankLines(byte[] bytes) {
        def temp = new ByteArrayOutputStream()
        byte carriageReturn = (byte) '\r'
        byte lineBreak = (byte) '\n'
        for (int idx = 0; idx < bytes.length; idx++) {
            byte current = bytes[idx]
            boolean skip = false
            if (current == carriageReturn) {
                if (idx + 2 < bytes.length) {
                    if (bytes[idx + 1] == lineBreak && bytes[idx + 2] == carriageReturn) {
                        skip = true
                    }
                }
            } else if (current == lineBreak) {
                if (idx + 1 < bytes.length) {
                    if (bytes[idx + 1] == carriageReturn) {
                        skip = true
                    }
                }
            }
            if (!skip) {
                temp.write(current)
            }
        }
        return temp.toByteArray()
    }

    private static java.util.jar.Manifest readJavaManifest(File file) {
        (java.util.jar.Manifest) file.withInputStream { new java.util.jar.Manifest(it) }
    }

    private static void writeJavaManifest(java.util.jar.Manifest manifest, File file) {
        file.withOutputStream { manifest.write(it) }
    }

    private static Manifest readAntManifest(File file) {
        (Manifest) file.withReader('UTF-8') { new Manifest(it) }
    }
}
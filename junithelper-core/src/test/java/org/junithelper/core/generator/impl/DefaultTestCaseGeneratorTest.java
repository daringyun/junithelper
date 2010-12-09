package org.junithelper.core.generator.impl;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.junithelper.core.config.Configulation;
import org.junithelper.core.config.JUnitVersion;
import org.junithelper.core.meta.AccessModifier;
import org.junithelper.core.meta.ClassMeta;
import org.junithelper.core.meta.TestCaseMeta;
import org.junithelper.core.meta.TestMethodMeta;
import org.junithelper.core.meta.extractor.ClassMetaExtractor;
import org.junithelper.core.util.IOUtil;
import org.junithelper.core.util.UniversalDetectorUtil;

public class DefaultTestCaseGeneratorTest {

	Configulation config = new Configulation();
	DefaultTestCaseGenerator target = new DefaultTestCaseGenerator(config);
	ClassMetaExtractor classMetaExtractor = new ClassMetaExtractor(config);

	@Test
	public void type() throws Exception {
		assertNotNull(DefaultTestCaseGenerator.class);
	}

	@Test
	public void instantiation() throws Exception {
		Configulation config = null;
		DefaultTestCaseGenerator target = new DefaultTestCaseGenerator(config);
		assertNotNull(target);
	}

	@Test
	public void initialize_A$ClassMeta() throws Exception {
		String sourceCodeString = "package hoge.foo; import java.util.List; public class Sample { public Sample() {}\r\n public int doSomething(String str, long longValue) throws Throwable { System.out.println(\"aaaa\") } }";
		ClassMeta targetClassMeta = classMetaExtractor
				.extract(sourceCodeString);
		target.initialize(targetClassMeta);
	}

	@Test
	public void getNewTestCaseMeta_A$() throws Exception {
		String sourceCodeString = "package hoge.foo; import java.util.List; public class Sample { public Sample() {}\r\n public int doSomething(String str, long longValue) throws Throwable { System.out.println(\"aaaa\") } }";
		ClassMeta targetClassMeta = classMetaExtractor
				.extract(sourceCodeString);
		target.initialize(targetClassMeta);
		TestCaseMeta actual = target.getNewTestCaseMeta();
		assertEquals("Sample", actual.target.name);
		assertEquals(1, actual.target.constructors.size());
		assertEquals(1, actual.target.methods.size());
		assertEquals("doSomething", actual.target.methods.get(0).name);
	}

	@Test
	public void getNewTestCaseMeta_A$_2() throws Exception {
		String encoding = UniversalDetectorUtil
				.getDetectedEncoding(IOUtil
						.getResourceAsStream("parser/impl/DefaultTestCaseGenerator.txt"));
		String sourceCodeString = IOUtil
				.readAsString(
						IOUtil.getResourceAsStream("parser/impl/DefaultTestCaseGenerator.txt"),
						encoding);
		ClassMeta targetClassMeta = classMetaExtractor
				.extract(sourceCodeString);
		target.initialize(targetClassMeta);
		TestCaseMeta actual = target.getNewTestCaseMeta();
		assertEquals("DefaultTestCaseGenerator", actual.target.name);
		assertEquals(1, actual.target.constructors.size());
		assertEquals(6, actual.target.methods.size());
	}

	@Test
	public void getNewTestCaseMeta_A$_3() throws Exception {
		String encoding = UniversalDetectorUtil
				.getDetectedEncoding(IOUtil
						.getResourceAsStream("parser/impl/DefaultTestCaseGenerator.txt"));
		String sourceCodeString = IOUtil.readAsString(
				IOUtil.getResourceAsStream("parser/impl/ObjectUtil.txt"),
				encoding);
		ClassMeta targetClassMeta = classMetaExtractor
				.extract(sourceCodeString);
		target.initialize(targetClassMeta);
		TestCaseMeta actual = target.getNewTestCaseMeta();
		assertEquals("ObjectUtil", actual.target.name);
		assertEquals(1, actual.target.constructors.size());
		assertEquals(AccessModifier.Private,
				actual.target.constructors.get(0).accessModifier);
		assertEquals(2, actual.target.methods.size());
	}

	@Test
	public void getLackingTestMethodMetaList_A$String() throws Exception {
		String sourceCodeString = "package hoge.foo; import java.util.List; public class Sample { public Sample() {}\r\n public int doSomething(String str, long longValue) throws Throwable { System.out.println(\"aaaa\") } public void overload(String str) { } public void overload(String str, Object obj) { } }";
		ClassMeta targetClassMeta = classMetaExtractor
				.extract(sourceCodeString);
		target.initialize(targetClassMeta);
		String currentTestCaseSourceCode = "package hoge.foo; public class SampleTest extends TestCase { public void test_overload_A$String$Object() throws Exception { } }";
		List<TestMethodMeta> actual = target
				.getLackingTestMethodMetaList(currentTestCaseSourceCode);
		assertEquals(4, actual.size());
		assertEquals(true, actual.get(0).isTypeTest);
		assertEquals(true, actual.get(1).isInstantiationTest);
		assertEquals("doSomething", actual.get(2).methodMeta.name);
		assertEquals("overload", actual.get(3).methodMeta.name);
	}

	@Test
	public void getNewTestCaseSourceCode_A$() throws Exception {
		String sourceCodeString = "package hoge.foo; import java.util.List; public class Sample { public Sample() {}\r\n public int doSomething(String str, long longValue) throws Throwable { System.out.println(\"aaaa\") } }";
		ClassMeta targetClassMeta = classMetaExtractor
				.extract(sourceCodeString);
		target.initialize(targetClassMeta);
		String actual = target.getNewTestCaseSourceCode();
		String expected = "package hoge.foo;\r\n\r\nimport static org.junit.Assert.*;\r\nimport org.junit.Test;\r\n\r\nimport java.util.List;\r\n\r\npublic class SampleTest {\r\n\r\n	@Test\r\n	public void type() throws Exception {\r\n		// TODO auto-generated by JUnit Helper.\r\n		assertNotNull(Sample.class);\r\n	}\r\n\r\n	@Test\r\n	public void instantiation() throws Exception {\r\n		// TODO auto-generated by JUnit Helper.\r\n		Sample target = new Sample();\r\n		assertNotNull(target);\r\n	}\r\n\r\n	@Test\r\n	public void doSomething_A$String$long() throws Exception {\r\n		// TODO auto-generated by JUnit Helper.\r\n		Sample target = new Sample();\r\n		String str = null;\r\n		long longValue = 0L;\r\n		int actual = target.doSomething(str, longValue);\r\n		int expected = 0;\r\n		assertEquals(expected, actual);\r\n	}\r\n\r\n}\r\n";
		assertEquals(expected, actual);
	}

	@Test
	public void getTestCaseSourceCodeWithLackingTestMethod_A$String()
			throws Exception {
		String sourceCodeString = "package hoge.foo; import java.util.List; public class Sample { public Sample() {}\r\n public int doSomething(String str, long longValue) throws Throwable { System.out.println(\"aaaa\") } }";
		ClassMeta targetClassMeta = classMetaExtractor
				.extract(sourceCodeString);
		target.initialize(targetClassMeta);
		String currentTestCaseSourceCode = "package hoge.foo;\r\n\r\nimport java.util.List;\r\n\r\npublic class SampleTest {\r\n\r\n}\r\n";
		String actual = target
				.getTestCaseSourceCodeWithLackingTestMethod(currentTestCaseSourceCode);
		String expected = "package hoge.foo;\r\n\r\nimport static org.junit.Assert.*;\r\nimport org.junit.Test;\r\n\r\nimport java.util.List;\r\n\r\npublic class SampleTest {\r\n\r\n	@Test\r\n	public void type() throws Exception {\r\n		// TODO auto-generated by JUnit Helper.\r\n		assertNotNull(Sample.class);\r\n	}\r\n\r\n	@Test\r\n	public void instantiation() throws Exception {\r\n		// TODO auto-generated by JUnit Helper.\r\n		Sample target = new Sample();\r\n		assertNotNull(target);\r\n	}\r\n\r\n	@Test\r\n	public void doSomething_A$String$long() throws Exception {\r\n		// TODO auto-generated by JUnit Helper.\r\n		Sample target = new Sample();\r\n		String str = null;\r\n		long longValue = 0L;\r\n		int actual = target.doSomething(str, longValue);\r\n		int expected = 0;\r\n		assertEquals(expected, actual);\r\n	}\r\n\r\n}\r\n";
		assertEquals(expected, actual);
	}

	@Test
	public void initialize_A$String() throws Exception {
		String targetSourceCodeString = "package hoge; public class HogeHoge { }";
		DefaultTestCaseGenerator actual = target
				.initialize(targetSourceCodeString);
		assertNotNull(actual);
	}

	@Test
	public void getUnifiedVersionTestCaseSourceCode_A$String$JUnitVersion()
			throws Exception {
		String currentTestCaseSourceCode = "package hoge;\r\nimport org.junit.Test;\r\n\r\npublic class SampleTest {\r\n\r\n\t@Test\r\n\tpublic void hogehoge() throws Excpetion {\r\n\t}\r\n\r\n }";
		String sourceCodeString = "package hoge.foo; import java.util.List; public class Sample { public Sample() {}\r\n public int doSomething(String str, long longValue) throws Throwable { System.out.println(\"aaaa\") } }";
		ClassMeta targetClassMeta = classMetaExtractor
				.extract(sourceCodeString);
		target.initialize(targetClassMeta);
		JUnitVersion version = JUnitVersion.version3;
		String actual = target.getUnifiedVersionTestCaseSourceCode(
				currentTestCaseSourceCode, version);
		String expected = "package hoge;\r\n\r\nimport java.util.List;\r\nimport junit.framework.TestCase;\r\nimport org.junit.Test;\r\n\r\npublic class SampleTest extends TestCase {\r\n\r\n\tpublic void test_hogehoge() throws Excpetion {\r\n\t}\r\n\r\n }";
		assertEquals(expected, actual);
	}

	@Test
	public void addRequiredImportList_A$String() throws Exception {
		String sourceCodeString = "package hoge.foo;\r\nimport java.util.List;\r\npublic class Sample {\r\n\r\n\tpublic Sample() {}\r\n\r\n\tpublic int doSomething(String str, long longValue) throws Throwable {\r\n\t\tSystem.out.println(\"aaaa\");\r\n\t}\r\n}";
		ClassMeta targetClassMeta = classMetaExtractor
				.extract(sourceCodeString);
		target.initialize(targetClassMeta);
		String src = "package hoge;\r\n\r\n\r\npublic class SampleTest extends TestCase {\r\n\r\n}";
		String actual = target.addRequiredImportList(src);
		String expected = "package hoge;\r\n\r\nimport java.util.List;\r\nimport static org.junit.Assert.*;\r\nimport org.junit.Test;\r\n\r\n\r\npublic class SampleTest extends TestCase {\r\n\r\n}";
		assertEquals(expected, actual);
	}

	@Test
	public void addRequiredImportList_A$String$Configulation() throws Exception {
		String sourceCodeString = "package hoge.foo;\r\nimport java.util.List;\r\npublic class Sample {\r\n\r\n\tpublic Sample() {}\r\n\r\n\tpublic int doSomething(String str, long longValue) throws Throwable {\r\n\t\tSystem.out.println(\"aaaa\");\r\n\t}\r\n}";
		ClassMeta targetClassMeta = classMetaExtractor
				.extract(sourceCodeString);
		target.initialize(targetClassMeta);
		String src = "package hoge;\r\n\r\n\r\npublic class SampleTest extends TestCase {\r\n\r\n}";
		String actual = target.addRequiredImportList(src, config);
		String expected = "package hoge;\r\n\r\nimport java.util.List;\r\nimport static org.junit.Assert.*;\r\nimport org.junit.Test;\r\n\r\n\r\npublic class SampleTest extends TestCase {\r\n\r\n}";
		assertEquals(expected, actual);
	}

	@Test
	public void appendIfNotExists_A$StringBuilder$String$String_alradyExists()
			throws Exception {
		StringBuilder buf = new StringBuilder();
		String src = "package hoge.foo;\r\nimport junit.framework.TestCase;\r\n\r\npublic class Sample {\r\n\r\n}";
		String importLine = "import junit.framework.TestCase;";
		target.appendIfNotExists(buf, src, importLine);
		assertEquals("", buf.toString());
	}

	@Test
	public void appendIfNotExists_A$StringBuilder$String$String_notExists()
			throws Exception {
		StringBuilder buf = new StringBuilder();
		String src = "package hoge.foo;\r\nimport junit.framework.TestCase;\r\n\r\npublic class Sample {\r\n\r\n}";
		String importLine = "import org.junit.Test;";
		target.appendIfNotExists(buf, src, importLine);
		target.appendIfNotExists(buf, src, importLine);
		assertEquals("import org.junit.Test;\r\nimport org.junit.Test;\r\n",
				buf.toString());
	}

}
/* 
 * Copyright 2009-2010 junithelper.org. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License. 
 */
package org.junithelper.core.meta.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junithelper.core.config.Configulation;
import org.junithelper.core.constant.RegExp;
import org.junithelper.core.constant.StringValue;
import org.junithelper.core.filter.TrimFilterUtil;
import org.junithelper.core.meta.AccessModifier;
import org.junithelper.core.meta.ClassMeta;
import org.junithelper.core.meta.ExceptionMeta;
import org.junithelper.core.meta.MethodMeta;
import org.junithelper.core.parser.convert.TypeNameConverter;
import org.junithelper.core.parser.detect.AccessModifierDetector;

public class MethodMetaExtractor {

	private Configulation config;
	private AccessModifierDetector accessModifierDetector = new AccessModifierDetector();
	private ClassMeta classMeta;

	public MethodMetaExtractor(Configulation config) {
		this.config = config;
	}

	public MethodMetaExtractor initialize(String sourceCodeString) {
		return initialize(null, sourceCodeString);
	}

	public MethodMetaExtractor initialize(ClassMeta classMeta) {
		this.classMeta = classMeta;
		return this;
	}

	public MethodMetaExtractor initialize(ClassMeta classMeta,
			String sourceCodeString) {
		if (classMeta == null) {
			this.classMeta = new ClassMetaExtractor(config)
					.extract(sourceCodeString);
		} else {
			this.classMeta = classMeta;
		}
		return this;
	}

	public List<MethodMeta> extract(String sourceCodeString) {
		List<MethodMeta> dest = new ArrayList<MethodMeta>();
		sourceCodeString = TrimFilterUtil.doAllFilters(sourceCodeString);
		// -----------------
		// for method signature
		Matcher mat = RegExp.PatternObject.MethodSignatureArea
				.matcher(sourceCodeString);
		while (mat.find()) {
			MethodMeta meta = new MethodMeta();
			String methodSignatureArea = mat.group(0)
					.replaceAll(StringValue.CarriageReturn, StringValue.Empty)
					.replaceAll(StringValue.LineFeed, StringValue.Space);
			// -----------------
			// skip constructors
			if (methodSignatureArea.matches(RegExp.Anything_ZeroOrMore_Min
					+ RegExp.WhiteSpace.Consecutive_OneOrMore_Max
					+ classMeta.name + "\\(" + RegExp.Anything_ZeroOrMore_Min
					+ "\\)" + RegExp.Anything_ZeroOrMore_Min)) {
				continue;
			}
			// -----------------
			// skip not method signature
			String methodSignatureAreaWithoutAccessModifier = trimAccessModifierFromMethodSignatureArea(methodSignatureArea);
			Matcher matcherGrouping = RegExp.PatternObject.MethodSignatureWithoutAccessModifier_Group
					.matcher(StringValue.Space
							+ methodSignatureAreaWithoutAccessModifier);
			if (!matcherGrouping.find()) {
				continue;
			}

			// -----------------
			// is static method
			if (methodSignatureArea.matches(RegExp.Anything_ZeroOrMore_Min
					+ RegExp.WhiteSpace.Consecutive_OneOrMore_Max + "static"
					+ RegExp.WhiteSpace.Consecutive_OneOrMore_Max
					+ RegExp.Anything_ZeroOrMore_Min)) {
				meta.isStatic = true;
			}
			// -----------------
			// access modifier
			meta.accessModifier = getAccessModifier(methodSignatureArea);
			// -----------------
			// return type
			String returnTypeFull = matcherGrouping.group(1)
					.replaceAll("final ", StringValue.Empty).split("\\s+")[0]
					.trim();
			// generics
			Matcher toGenericsMatcherForReturn = Pattern.compile(
					RegExp.Generics_Group).matcher(returnTypeFull);
			while (toGenericsMatcherForReturn.find()) {
				String[] generics = toGenericsMatcherForReturn.group()
						.replaceAll("<", StringValue.Empty)
						.replaceAll(">", StringValue.Empty)
						.split(StringValue.Comma);
				for (String generic : generics) {
					generic = new TypeNameConverter(config).toCompilableType(
							classMeta.packageName, generic,
							classMeta.importedList).trim();
					meta.returnType.generics.add(generic);
				}
			}
			String returnTypeName = returnTypeFull.replace(RegExp.Generics,
					StringValue.Empty);
			if (!returnTypeName.equals("void")) {
				meta.returnType.name = new TypeNameConverter(config)
						.toCompilableType(classMeta.packageName,
								returnTypeName, meta.returnType.generics,
								classMeta.importedList).trim();
				meta.returnType.nameInMethodName = new TypeNameConverter(config)
						.toAvailableInMethodName(meta.returnType.name);
			}
			// -----------------
			// method name
			meta.name = matcherGrouping.group(2);
			// -----------------
			// args
			String argsAreaString = matcherGrouping.group(3);
			ArgTypeMetaExtractor argTypeMetaExtractor = new ArgTypeMetaExtractor(
					config);
			argTypeMetaExtractor.initialize(classMeta)
					.doExtract(argsAreaString);
			meta.argNames = argTypeMetaExtractor.getExtractedNameList();
			meta.argTypes = argTypeMetaExtractor.getExtractedMetaList();
			// -----------------
			// is accessor method or not
			String fieldName = null;
			String fieldType = null;
			if (meta.name.matches("^set.+")) {
				// target field name
				fieldName = meta.name.substring(3);
				if (meta.argTypes.size() > 0) {
					fieldType = meta.argTypes.get(0).name;
				}
			} else if (meta.name.matches("^get.+")) {
				// target field name
				fieldName = meta.name.substring(3);
				fieldType = meta.returnType.name;
			} else if (meta.name.matches("^is.+")) {
				// target field name
				fieldName = meta.name.substring(2);
				fieldType = meta.returnType.name;
			}
			if (fieldName != null && fieldType != null) {
				meta.isAccessor = isPrivateFieldExists(fieldType, fieldName,
						sourceCodeString);
			}
			// -----------------
			// throws exception
			String throwsExceptions = matcherGrouping.group(4);
			if (throwsExceptions != null) {
				String[] exceptions = throwsExceptions.replaceAll(
						"throws" + RegExp.WhiteSpace.Consecutive_OneOrMore_Max,
						StringValue.Empty).split(StringValue.Comma);
				for (String exception : exceptions) {
					exception = exception.trim();
					ExceptionMeta exceptionMeta = new ExceptionMeta();
					exceptionMeta.name = exception;
					exceptionMeta.nameInMethodName = new TypeNameConverter(
							config).toAvailableInMethodName(exception);
					meta.throwsExceptions.add(exceptionMeta);
				}
			}
			dest.add(meta);
		}
		return dest;
	}

	boolean isPrivateFieldExists(String fieldType, String fieldName,
			String sourceCodeString) {
		// field name
		String regExpForFieldNameArea = fieldName.substring(0, 1).toLowerCase()
				+ fieldName.substring(1);
		// field type
		// considering array, generics comma
		String regExpForFieldTypeArea = fieldType.replaceAll("\\[", "\\\\[")
				.replaceAll("\\]", "\\\\]").replaceAll(",", "\\\\s*,\\\\s*");
		String regExpForPrivateFieldThatHasAccessors = ".*?private\\s+"
				+ regExpForFieldTypeArea + "(" + RegExp.Generics + ")*"
				+ RegExp.WhiteSpace.Consecutive_OneOrMore_Max
				+ regExpForFieldNameArea + ".+";
		return sourceCodeString.replaceAll(RegExp.CRLF, StringValue.Empty)
				.matches(regExpForPrivateFieldThatHasAccessors);
	}

	AccessModifier getAccessModifier(String methodSignatureArea) {
		if (accessModifierDetector.isPublic(methodSignatureArea)) {
			return AccessModifier.Public;
		} else if (accessModifierDetector.isProtected(methodSignatureArea)) {
			return AccessModifier.Protected;
		} else if (accessModifierDetector.isPackageLocal(methodSignatureArea)) {
			return AccessModifier.PackageLocal;
		} else if (accessModifierDetector.isPrivate(methodSignatureArea)) {
			return AccessModifier.Private;
		} else {
			return AccessModifier.Public;
		}
	}

	String trimAccessModifierFromMethodSignatureArea(String methodSignatureArea) {
		String regExpForAccessModifier_public = AccessModifierDetector.RegExp.Prefix
				+ "public" + "\\s+";
		String regExpForAccessModifier_protected = AccessModifierDetector.RegExp.Prefix
				+ "protected" + "\\s+";
		String methodSignatureAreaWithoutAccessModifier = methodSignatureArea
				.replaceAll(StringValue.Tab, StringValue.Space)
				.replaceAll(regExpForAccessModifier_public, StringValue.Space)
				.replaceAll(regExpForAccessModifier_protected,
						StringValue.Space)
				.replaceAll("\\sfinal\\s", StringValue.Space);
		return methodSignatureAreaWithoutAccessModifier;
	}

}
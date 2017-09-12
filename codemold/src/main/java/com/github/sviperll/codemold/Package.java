/*
 * Copyright (c) 2016, Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation and/or
 *     other materials provided with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.github.sviperll.codemold;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public final class Package implements Model {
    private static final Logger logger = Logger.getLogger(Package.class.getName());

    static Package createTopLevelPackage(CodeMold codeModel) {
        return new Package(codeModel, "");
    }

    private final CodeMold codeModel;
    private final String name;
    private final Package parent;
    private final Map<String, ObjectDefinition> classes = new TreeMap<>();
    private final Map<String, Package> packages = new TreeMap<>();

    private Package(CodeMold codeModel, String name) {
        this(codeModel, name, null);
    }

    private Package(CodeMold codeModel, String name, Package parent) {
        this.codeModel = codeModel;
        this.name = name;
        this.parent = parent;
    }

    /** This package's parent.
     * Throws UnsupportedOperationException for root/default package
     * @see Package#isRootPackage()
     * @throws UnsupportedOperationException
     * @return This package's parent.
     */
    @Nonnull
    public Package getParent() {
        return parent;
    }

    /** Is it root/default package.
     * @return Is it root/default package.
     */
    public boolean isRootPackage() {
        return parent == null;
    }

    @Nonnull
    public ClassBuilder<PackageLevelBuilder> createClass(String className) throws CodeMoldException {
        if (getReference(className).isPresent())
            throw new CodeMoldException(packageAsNamePrefix() + className + " already defined");
        PackageLevelBuilder membershipBuilder = new PackageLevelBuilder(this);
        ClassBuilder<PackageLevelBuilder> result = new ClassBuilder<>(membershipBuilder, className);
        classes.put(className, result.definition());
        return result;
    }

    @Nonnull
    public InterfaceBuilder<PackageLevelBuilder> createInterface(String className) throws CodeMoldException {
        if (getReference(className).isPresent())
            throw new CodeMoldException(packageAsNamePrefix() + className + " already defined");
        PackageLevelBuilder membershipBuilder = new PackageLevelBuilder(this);
        InterfaceBuilder<PackageLevelBuilder> result = new InterfaceBuilder<>(membershipBuilder, className);
        classes.put(className, result.definition());
        return result;
    }

    @Nonnull
    public EnumBuilder<PackageLevelBuilder> createEnum(String className) throws CodeMoldException {
        if (getReference(className).isPresent())
            throw new CodeMoldException(packageAsNamePrefix() + className + " already defined");
        PackageLevelBuilder membershipBuilder = new PackageLevelBuilder(this);
        EnumBuilder<PackageLevelBuilder> result = new EnumBuilder<>(membershipBuilder, className);
        classes.put(className, result.definition());
        return result;
    }

    @Nonnull
    public AnnotationDefinitionBuilder<PackageLevelBuilder> createAnnotationDefinition(String className) throws CodeMoldException {
        if (getReference(className).isPresent())
            throw new CodeMoldException(packageAsNamePrefix() + className + " already defined");
        PackageLevelBuilder membershipBuilder = new PackageLevelBuilder(this);
        AnnotationDefinitionBuilder<PackageLevelBuilder> result = new AnnotationDefinitionBuilder<>(membershipBuilder, className);
        classes.put(className, result.definition());
        return result;
    }

    @Nonnull
    Optional<ObjectDefinition> getReference(String relativelyQualifiedName) {
        int index = relativelyQualifiedName.indexOf('.');
        if (index == 0)
            throw new IllegalArgumentException(packageAsNamePrefix() + relativelyQualifiedName + " illegal name");
        boolean needsToGoDeeper = index >= 0;
        String simpleName = !needsToGoDeeper ? relativelyQualifiedName : relativelyQualifiedName.substring(0, index);
        String qualifiedName = packageAsNamePrefix() + simpleName;
        Optional<ObjectDefinition> result = Optional.ofNullable(classes.get(simpleName));
        if (!result.isPresent()) {
            Reflection reflection = new Reflection(codeModel);
            result = reflection.createNewReflectedClassObjectDefinition(this, qualifiedName);
        }
        if (!result.isPresent()) {
            Optional<Mirror> optionalMirror = codeModel.createMirror();
            result = optionalMirror.flatMap(mirror -> mirror.createNewMirroredTypeObjectDefinition(this, qualifiedName));
        }
        result.ifPresent(definition -> {
            classes.put(simpleName, definition);
        });
        if (!needsToGoDeeper) {
            return result;
        } else {
            String childRelativeName = relativelyQualifiedName.substring(simpleName.length() + 1);
            if (result.isPresent())
                return result.flatMap(definition -> definition.getReference(childRelativeName));
            else {
                Package childPackage = packages.get(simpleName);
                if (childPackage == null) {
                    childPackage = new Package(codeModel, packageAsNamePrefix() + simpleName, this);
                    packages.put(simpleName, childPackage);
                }
                return childPackage.getReference(childRelativeName);
            }
        }
    }

    @Nonnull
    public String qualifiedName() {
        return name;
    }

    @Nonnull
    private String packageAsNamePrefix() {
        return name.isEmpty() ? "" : name + ".";
    }

    @Nonnull
    @Override
    public CodeMold getCodeMold() {
        return codeModel;
    }

    @Nonnull
    Package getChildPackageBySuffix(String suffix) throws CodeMoldException {
        int index = suffix.indexOf('.');
        if (index == 0)
            throw new IllegalArgumentException(packageAsNamePrefix() + suffix + " illegal package name");
        boolean isChild = index < 0;
        String childSuffix = isChild ? suffix : suffix.substring(0, index);
        if (classes.containsKey(childSuffix))
            throw new CodeMoldException(packageAsNamePrefix() + childSuffix + " is a class, but package expected");
        Package childPackage = packages.get(childSuffix);
        if (childPackage == null) {
            CodeMold.validateSimpleName(childSuffix);
            childPackage = new Package(codeModel, packageAsNamePrefix() + childSuffix, this);
            packages.put(childSuffix, childPackage);
        }
        if (isChild)
            return childPackage;
        else
            return childPackage.getChildPackageBySuffix(suffix.substring(index + 1));
    }
}

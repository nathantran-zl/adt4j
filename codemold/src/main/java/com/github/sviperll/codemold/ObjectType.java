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

import com.github.sviperll.codemold.render.Renderable;
import com.github.sviperll.codemold.render.Renderer;
import com.github.sviperll.codemold.render.RendererContext;
import com.github.sviperll.codemold.util.CMCollections;
import com.github.sviperll.codemold.util.Snapshot;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class ObjectType extends GenericType<ObjectType, ObjectDefinition>
        implements Renderable, Type {
    private AnyType type = null;
    private Map<? extends MethodSignature, ? extends MethodType> methods = null;
    private Map<? extends ObjectDefinition, ? extends ObjectType> interfaces = null;
    private List<? extends ConstructorType> constructors = null;
    private List<? extends ObjectType> supertypes = null;
    private ObjectType superClass = null;

    ObjectType(GenericType.Implementation<ObjectType, ObjectDefinition> implementation) {
        super(implementation);
    }

    @Nonnull
    @Override
    public final AnyType asAny() {
        if (type == null)
            type = AnyType.wrapObjectType(new Wrappable());
        return type;
    }

    @Nonnull
    public final IntersectionType intersection(ObjectType that) {
        return new IntersectionType(Arrays.asList(this, that));
    }

    @Nonnull
    public final Expression instanceofOp(Expression expression) throws CodeMoldException {
        return expression.instanceofOp(this);
    }

    final boolean containsWildcards() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public final boolean sameDefinition(ObjectType that) {
        return definition() == that.definition();
    }

    /**
     * All methods supported by this object.
     * Inherited methods are included.
     * @return all methods supported by this object
     */
    @Nonnull
    public final Collection<? extends MethodType> methods() {
        if (methods == null) {
            Map<MethodSignature, MethodType> methodsBuilder = CMCollections.newHashMap();
            interfaces().stream().forEach((supertype) -> {
                supertype.methods().stream().forEach((method) -> {
                    methodsBuilder.put(method.signature(), method);
                });
            });
            if (!isJavaLangObject()) {
                superClass().methods().stream().forEach((method) -> {
                    methodsBuilder.put(method.signature(), method);
                });
            }
            definition().methods().stream().map(definition -> definition.isStatic() ? definition.rawType() : definition.rawType(this)).forEach((method) -> {
                methodsBuilder.put(method.signature(), method);
            });
            methods = Snapshot.of(methodsBuilder);
        }
        return Snapshot.of(methods.values());
    }

    /**
     * All interfaces supported by this object.
     * Interfaces are listed regardless wheather they are explicitly implemented or inherited somehow.
     * @return All interfaces supported by this object.
     */
    public Collection<? extends ObjectType> interfaces() {
        if (interfaces == null) {
            Map<ObjectDefinition, ObjectType> interfacesBuilder = new HashMap<>();
            if (!isJavaLangObject()) {
                superClass().interfaces().stream().forEach((iface) -> {
                    interfacesBuilder.put(iface.definition(), iface);
                });
            }
            definition().implementsInterfaces().forEach(declaredInterface -> {
                ObjectType iface = declaredInterface.substitute(definitionEnvironment());
                interfacesBuilder.put(iface.definition(), iface);
                iface.interfaces().forEach((subinterface) -> {
                    interfacesBuilder.put(subinterface.definition(), subinterface);
                });
            });
            interfaces = Snapshot.of(interfacesBuilder);
        }
        return Snapshot.of(interfaces.values());
    }

    /**
     * Directly extended class.
     * For interfaces superClass is java.lang.Object.
     * @return Directly extended class
     */
    public ObjectType superClass() {
        if (superClass == null) {
            superClass = definition().extendsClass().substitute(definitionEnvironment());
        }
        return superClass;
    }

    /**
     * All directly extended/implemented types.
     * For interfaces #supertypes() is a list of extended interfaces or
     * single java.lang.Object type when no interfaces are extended.
     * @return All directly extended/implemented types.
     */
    public Collection<? extends ObjectType> supertypes() {
        if (supertypes == null) {
            List<ObjectType> supertypesBuilder = CMCollections.newArrayList();
            if (definition().kind().isInterface() && definition().implementsInterfaces().isEmpty()) {
                supertypesBuilder.add(definition().getCodeMold().objectType());
            } else {
                if (!isJavaLangObject()) {
                    supertypesBuilder.add(superClass());
                }
                definition().implementsInterfaces().forEach((iface) -> {
                    supertypesBuilder.add(iface.substitute(definitionEnvironment()));
                });
            }
            supertypes = Snapshot.of(supertypesBuilder);
        }
        return Snapshot.of(supertypes);
    }

    @Nonnull
    public final Collection<? extends ConstructorType> constructors() {
        if (constructors == null) {
            List<ConstructorType> constructorsBuilder = CMCollections.newArrayList();
            definition().constructors().forEach((definition) -> {
                constructorsBuilder.add(definition.rawType(this));
            });
            constructors = Snapshot.of(constructorsBuilder);
        }
        return Snapshot.of(constructors);
    }

    /**
     * Class instantiation.
     *
     * When this is raw-type diamond operator is generated.
     * @see #rawInstantiation(java.util.List) for raw-type instantiation.
     * @param arguments constructor arguments
     * @return Class instantiation
     */
    @Nonnull
    public Expression instantiation(final List<? extends Expression> arguments) {
        return Expression.instantiation(this, arguments);
    }

    /**
     * Anonymous class instantiation.
     *
     * Expression context is either a
     * com.github.sviperll.codemodel.BlockBuilder or
     * com.github.sviperll.codemodel.FieldBuilder.
     * It depends on where this anonymous class is going to be used.
     * com.github.sviperll.codemodel.FieldBuilder
     * should be used when anonymous class is used in field initializer.
     * <p>
     * Use anonymousClassDefinition argument to actually define anonymous class.
     *
     * @param arguments constructor arguments
     * @param context expression context
     * @param anonymousClassDefinition
     * @return Object instantiation (new-expression)
     */
    @Nonnull
    public Expression instantiation(final List<? extends Expression> arguments, ExpressionContext context, Consumer<? super AnonymousClassBuilder> anonymousClassDefinition) {
        return Expression.instantiation(this, arguments, context, anonymousClassDefinition);
    }

    /**
     * Raw-type instantiation.
     *
     * @see #instantiation(java.util.List) if you need diamond operator.
     * @param arguments constructor arguments
     * @return Raw-type instantiation
     */
    @Nonnull
    public Expression rawInstantiation(final List<? extends Expression> arguments) {
        return Expression.rawInstantiation(this, arguments);
    }

    public boolean isJavaLangObject() {
        return definition().isJavaLangObject();
    }

    @Nonnull
    @Override
    public Renderer createRenderer(final RendererContext context) {
        return () -> {
            if (isRaw())
                context.appendQualifiedClassName(definition().qualifiedTypeName());
            else {
                context.appendRenderable(erasure());
                Iterator<? extends AnyType> iterator = typeArguments().iterator();
                if (iterator.hasNext()) {
                    context.appendText("<");
                    context.appendRenderable(iterator.next());
                    while (iterator.hasNext()) {
                        context.appendText(", ");
                        context.appendRenderable(iterator.next());
                    }
                    context.appendText(">");
                }
            }
        };
    }

    class Wrappable {
        private Wrappable() {
        }
        ObjectType value() {
            return ObjectType.this;
        }
    }
}

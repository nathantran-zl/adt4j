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
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public enum PrimitiveType implements Renderable, Type {
    BYTE, SHORT, INT, LONG,
    FLOAT, DOUBLE,
    CHAR,
    BOOLEAN;

    private AnyType type = null;


    public boolean isByte() {
        return this == BYTE;
    }

    public boolean isShort() {
        return this == SHORT;
    }

    public boolean isInteger() {
        return this == INT;
    }

    public boolean isLong() {
        return this == LONG;
    }

    public boolean isFloat() {
        return this == FLOAT;
    }

    public boolean isDouble() {
        return this == DOUBLE;
    }

    public boolean isBoolean() {
        return this == BOOLEAN;
    }

    public boolean isCharacter() {
        return this == CHAR;
    }

    @Nonnull
    @Override
    public AnyType asAny() {
        if (type == null)
            type = AnyType.wrapPrimitiveType(new Wrappable());
        return type;
    }

    @Nonnull
    @Override
    public Renderer createRenderer(final RendererContext context) {
        return () -> {
            context.appendText(name().toLowerCase(Locale.US));
        };
    }

    class Wrappable {
        private Wrappable() {
        }
        PrimitiveType value() {
            return PrimitiveType.this;
        }
    }

}

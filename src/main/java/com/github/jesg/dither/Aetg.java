package com.github.jesg.dither;

/*
 * #%L
 * dither
 * %%
 * Copyright (C) 2015 Jason Gowan
 * %%
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
 * #L%
 */

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

abstract class Aetg implements Iterator<Object[]> {

    public abstract boolean hasNext();
    public abstract Object[] next();

    public List<Object[]> toList() {
        final List<Object[]> results = new LinkedList<Object[]>();

        while(hasNext()) {
            final Object[] result = next();
            if(result != null) {
                results.add(result);
            }
        }

        return results;
    }

    public Object[][] toArray() {
        return toList().toArray(new Object[][]{});
    }

    public void remove() {}

}

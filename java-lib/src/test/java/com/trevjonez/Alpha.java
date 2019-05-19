/*
 *    Copyright 2019 Trevor Jones
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.trevjonez;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

public class Alpha {

    @Test
    public void passes() {
        Assert.assertTrue(true);
    }

    @Test
    public void fails() {
        Assert.assertTrue(false);
    }

    @Test
    public void skipped() {
        Assume.assumeTrue(false);
    }
}

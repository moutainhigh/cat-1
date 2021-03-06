/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zoo.cat.common.serializer;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import org.zoo.cat.annotation.CatSPI;
import org.zoo.cat.common.enums.SerializeEnum;
import org.zoo.cat.common.exception.CatException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * HessianSerializer.
 *
 * @author dzc
 */
@SuppressWarnings("unchecked")
@CatSPI("hessian")
public class HessianSerializer implements ObjectSerializer {

    @Override
    public byte[] serialize(final Object obj) throws CatException {
        Hessian2Output hos;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            hos = new Hessian2Output(bos);
            hos.writeObject(obj);
            hos.flush();
            return bos.toByteArray();
        } catch (IOException ex) {
            throw new CatException("Hessian serialize error " + ex.getMessage());
        }
    }

    @Override
    public <T> T deSerialize(final byte[] param, final Class<T> clazz) throws CatException {
        ByteArrayInputStream bios;
        try {
            bios = new ByteArrayInputStream(param);
            Hessian2Input his = new Hessian2Input(bios);
            return (T) his.readObject();
        } catch (IOException e) {
            throw new CatException("Hessian deSerialize error " + e.getMessage());
        }
    }
}

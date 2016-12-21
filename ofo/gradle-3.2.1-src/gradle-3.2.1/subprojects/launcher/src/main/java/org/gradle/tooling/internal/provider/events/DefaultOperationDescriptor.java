/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.tooling.internal.provider.events;

import org.gradle.tooling.internal.protocol.events.InternalOperationDescriptor;

import java.io.Serializable;

public class DefaultOperationDescriptor implements Serializable, InternalOperationDescriptor {
    private final Object id;
    private final String name;
    private final String displayName;
    private final Object parentId;

    public DefaultOperationDescriptor(Object id, String name, String displayName, Object parentId) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.parentId = parentId;
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Object getParentId() {
        return parentId;
    }
}
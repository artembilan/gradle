/*
 * Copyright 2013 the original author or authors.
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
package org.gradle.ide.visualstudio

import org.gradle.api.Incubating
import org.gradle.api.Plugin
import org.gradle.api.internal.artifacts.dsl.dependencies.ProjectFinder
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.tasks.Delete
import org.gradle.ide.visualstudio.internal.DefaultVisualStudioExtension
import org.gradle.ide.visualstudio.internal.rules.CreateVisualStudioModel
import org.gradle.ide.visualstudio.internal.rules.CreateVisualStudioTasks
import org.gradle.internal.reflect.Instantiator
import org.gradle.model.ModelRules
import org.gradle.model.internal.Inputs
import org.gradle.model.internal.ModelCreator
import org.gradle.nativebinaries.FlavorContainer
import org.gradle.nativebinaries.internal.resolve.RelativeProjectFinder
import org.gradle.nativebinaries.platform.PlatformContainer
import org.gradle.nativebinaries.plugins.NativeBinariesModelPlugin

import javax.inject.Inject

@Incubating
class VisualStudioPlugin implements Plugin<ProjectInternal> {
    private final Instantiator instantiator
    private final ModelRules modelRules

    @Inject
    VisualStudioPlugin(Instantiator instantiator, ModelRules modelRules) {
        this.instantiator = instantiator
        this.modelRules = modelRules
    }

    void apply(ProjectInternal project) {
        project.plugins.apply(NativeBinariesModelPlugin)

        project.modelRegistry.create("visualStudio", ["flavors", "platforms"], new VisualStudioExtensionFactory(instantiator, new RelativeProjectFinder(project), project.getFileResolver()))
        modelRules.rule(new CreateVisualStudioModel())
        modelRules.rule(new CreateVisualStudioTasks())

        def cleanTask = project.task("cleanVisualStudio", type: Delete) {
            delete "visualStudio"
        }
        cleanTask.group = "IDE"
    }

    private static class VisualStudioExtensionFactory implements ModelCreator<DefaultVisualStudioExtension> {
        private final Instantiator instantiator;
        private final ProjectFinder projectFinder;
        private final FileResolver fileResolver;

        public VisualStudioExtensionFactory(Instantiator instantiator, ProjectFinder projectFinder, FileResolver fileResolver) {
            this.instantiator = instantiator;
            this.projectFinder = projectFinder;
            this.fileResolver = fileResolver;
        }

        DefaultVisualStudioExtension create(Inputs inputs) {
            FlavorContainer flavors = inputs.get(0, FlavorContainer)
            PlatformContainer platforms = inputs.get(1, PlatformContainer)
            return instantiator.newInstance(DefaultVisualStudioExtension.class, instantiator, projectFinder, fileResolver, flavors, platforms);
        }

        Class<DefaultVisualStudioExtension> getType() {
            return DefaultVisualStudioExtension
        }
    }
}

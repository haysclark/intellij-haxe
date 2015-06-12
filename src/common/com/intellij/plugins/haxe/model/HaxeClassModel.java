/*
 * Copyright 2000-2013 JetBrains s.r.o.
 * Copyright 2014-2015 AS3Boyan
 * Copyright 2014-2014 Elias Ku
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.plugins.haxe.model;

import com.intellij.plugins.haxe.HaxeComponentType;
import com.intellij.plugins.haxe.lang.psi.*;
import com.intellij.plugins.haxe.model.type.HaxeTypeResolver;
import com.intellij.plugins.haxe.model.type.SpecificHaxeClassReference;
import com.intellij.plugins.haxe.model.type.SpecificTypeReference;
import com.intellij.plugins.haxe.util.HaxePsiUtils;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class HaxeClassModel {
  public HaxeClass haxeClass;

  public HaxeClassModel(HaxeClass haxeClass) {
    this.haxeClass = haxeClass;
  }

  public HaxeClassReferenceModel getParentClassReference() {
    List<HaxeType> list = haxeClass.getHaxeExtendsList();
    if (list.size() == 0) return null;
    return new HaxeClassReferenceModel(list.get(0));
  }

  static public boolean isValidClassName(String name) {
    return name.substring(0, 1).equals(name.substring(0, 1).toUpperCase());
  }

  public HaxeClassModel getParentClass() {
    final HaxeClassReferenceModel reference = this.getParentClassReference();
    return (reference != null) ? reference.getHaxeClass() : null;
  }

  public List<HaxeClassReferenceModel> getInterfaceExtendingInterfaces() {
    List<HaxeType> list = haxeClass.getHaxeExtendsList();
    List<HaxeClassReferenceModel> out = new ArrayList<HaxeClassReferenceModel>();
    for (HaxeType type : list) {
      out.add(new HaxeClassReferenceModel(type));
    }
    return out;
  }

  public List<HaxeClassReferenceModel> getImplementingInterfaces() {
    List<HaxeType> list = haxeClass.getHaxeImplementsList();
    List<HaxeClassReferenceModel> out = new ArrayList<HaxeClassReferenceModel>();
    for (HaxeType type : list) {
      out.add(new HaxeClassReferenceModel(type));
    }
    return out;
  }

  public boolean isExtern() {
    return haxeClass.isExtern();
  }

  public boolean isClass() {
    return !this.isAbstract() && (HaxeComponentType.typeOf(haxeClass) == HaxeComponentType.CLASS);
  }

  public boolean isInterface() {
    return HaxeComponentType.typeOf(haxeClass) == HaxeComponentType.INTERFACE;
  }

  public boolean isEnum() {
    return HaxeComponentType.typeOf(haxeClass) == HaxeComponentType.ENUM;
  }

  public boolean isTypedef() {
    return HaxeComponentType.typeOf(haxeClass) == HaxeComponentType.TYPEDEF;
  }

  public boolean isAbstract() {
    return haxeClass instanceof HaxeAbstractClassDeclaration;
  }

  // @TODO: Create AbstractHaxeClassModel extending this class for these methods?
  // @TODO: this should be properly parsed in haxe.bnf so searching for the underlying type is not required
  public HaxeType getAbstractUnderlyingType() {
    if (!isAbstract()) return null;
    PsiElement[] children = getPsi().getChildren();
    if (children.length >= 4) {
      if (children[2].getText().equals("(")) {
        if (children[3] instanceof HaxeType) {
          return (HaxeType)children[3];
        }
      }
    }
    return null;
  }

  // @TODO: this should be properly parsed in haxe.bnf so searching for to is not required
  public List<HaxeType> getAbstractToList() {
    if (!isAbstract()) return Collections.emptyList();
    List<HaxeType> types = new LinkedList<HaxeType>();
    for (HaxeIdentifier id : HaxePsiUtils.getChilds(haxeClass, HaxeIdentifier.class)) {
      if (id.getText().equals("to")) {
        PsiElement sibling = HaxePsiUtils.getNextSiblingNoSpaces(id);
        if (sibling instanceof HaxeType) {
          types.add((HaxeType)sibling);
        }
      }
    }
    return types;
  }

  // @TODO: this should be properly parsed in haxe.bnf so searching for from is not required
  public List<HaxeType> getAbstractFromList() {
    if (!isAbstract()) return Collections.emptyList();
    List<HaxeType> types = new LinkedList<HaxeType>();
    for (HaxeIdentifier id : HaxePsiUtils.getChilds(haxeClass, HaxeIdentifier.class)) {
      if (id.getText().equals("from")) {
        PsiElement sibling = HaxePsiUtils.getNextSiblingNoSpaces(id);
        if (sibling instanceof HaxeType) {
          types.add((HaxeType)sibling);
        }
      }
    }
    return types;
  }

  public boolean hasMethod(String name) {
    return getMethod(name) != null;
  }

  public boolean hasMethodSelf(String name) {
    HaxeMethodModel method = getMethod(name);
    if (method == null) return false;
    return (method.getDeclaringClass() == this);
  }

  public HaxeMethodModel getMethodSelf(String name) {
    HaxeMethodModel method = getMethod(name);
    if (method == null) return null;
    return (method.getDeclaringClass() == this) ? method : null;
  }

  public HaxeMethodModel getConstructorSelf() {
    return getMethodSelf("new");
  }

  public HaxeMethodModel getConstructor() {
    return getMethod("new");
  }

  public HaxeMethodModel getParentConstructor() {
    HaxeClassReferenceModel parentClass = getParentClassReference();
    if (parentClass == null) return null;
    return parentClass.getHaxeClass().getMethod("new");
  }

  public HaxeMemberModel getMember(String name) {
    final HaxeMethodModel method = getMethod(name);
    final HaxeFieldModel field = getField(name);
    return (method != null) ? method : field;
  }

  public HaxeFieldModel getField(String name) {
    HaxeVarDeclaration name1 = (HaxeVarDeclaration)haxeClass.findHaxeFieldByName(name);
    return name1 != null ? name1.getModel() : null;
  }

  public HaxeMethodModel getMethod(String name) {
    HaxeMethodPsiMixin name1 = (HaxeMethodPsiMixin)haxeClass.findHaxeMethodByName(name);
    return name1 != null ? name1.getModel() : null;
  }

  public List<HaxeMethodModel> getMethods() {
    List<HaxeMethodModel> models = new ArrayList<HaxeMethodModel>();
    for (HaxeMethod method : haxeClass.getHaxeMethods()) {
      models.add(method.getModel());
    }
    return models;
  }

  public List<HaxeMethodModel> getMethodsSelf() {
    List<HaxeMethodModel> models = new ArrayList<HaxeMethodModel>();
    for (HaxeMethod method : haxeClass.getHaxeMethods()) {
      if (method.getContainingClass() == this.haxeClass) models.add(method.getModel());
    }
    return models;
  }

  public List<HaxeMethodModel> getAncestorMethods() {
    List<HaxeMethodModel> models = new ArrayList<HaxeMethodModel>();
    for (HaxeMethod method : haxeClass.getHaxeMethods()) {
      if (method.getContainingClass() != this.haxeClass) models.add(method.getModel());
    }
    return models;
  }

  public HaxeClass getPsi() {
    return haxeClass;
  }

  public PsiElement getNamePsi() {
    return haxeClass.getNameIdentifier();
  }

  private HaxeDocumentModel _document = null;
  @NotNull
  public HaxeDocumentModel getDocument() {
    if (_document == null) _document = new HaxeDocumentModel(haxeClass);
    return _document;
  }

  public String getName() {
    return haxeClass.getName();
  }

  public void addMethodsFromPrototype(List<HaxeMethodModel> methods) {
    throw new NotImplementedException("Not implemented HaxeClassMethod.addMethodsFromPrototype() : check HaxeImplementMethodHandler");
  }

  public List<HaxeFieldModel> getFields() {
    HaxeClassBody body = HaxePsiUtils.getChild(haxeClass, HaxeClassBody.class);
    LinkedList<HaxeFieldModel> out = new LinkedList<HaxeFieldModel>();
    if (body != null) {
      for (HaxeVarDeclaration declaration : HaxePsiUtils.getChilds(body, HaxeVarDeclaration.class)) {
        out.add(new HaxeFieldModel(declaration));
      }
    }
    return out;
  }

  public Set<HaxeClassModel> getCompatibleTypes() {
    final Set<HaxeClassModel> output = new LinkedHashSet<HaxeClassModel>();
    writeCompatibleTypes(output);
    return output;
  }

  public void writeCompatibleTypes(Set<HaxeClassModel> output) {
    // Own
    output.add(this);

    final HaxeClassModel parentClass = this.getParentClass();

    // Parent classes
    if (parentClass != null) {
      if (!output.contains(parentClass)) {
        parentClass.writeCompatibleTypes(output);
      }
    }

    // Interfaces
    for (HaxeClassReferenceModel model : this.getImplementingInterfaces()) {
      if (model == null) continue;
      final HaxeClassModel aInterface = model.getHaxeClass();
      if (aInterface == null) continue;
      if (!output.contains(aInterface)) {
        aInterface.writeCompatibleTypes(output);
      }
    }

    // @CHECK abstract FROM
    for (HaxeType type : getAbstractFromList()) {
      final SpecificTypeReference aTypeRef = HaxeTypeResolver.getTypeFromType(type);
      if (aTypeRef instanceof SpecificHaxeClassReference) {
        ((SpecificHaxeClassReference)aTypeRef).getHaxeClassModel().writeCompatibleTypes(output);
      }
    }

    // @CHECK abstract TO
    for (HaxeType type : getAbstractToList()) {
      final SpecificTypeReference aTypeRef = HaxeTypeResolver.getTypeFromType(type);
      if (aTypeRef instanceof SpecificHaxeClassReference) {
        ((SpecificHaxeClassReference)aTypeRef).getHaxeClassModel().writeCompatibleTypes(output);
      }
    }
  }
}

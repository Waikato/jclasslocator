/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * ClassListerExamplePropertiesBasedClassList.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package nz.ac.waikato.cms.locator.example;

import nz.ac.waikato.cms.locator.ClassLister;
import nz.ac.waikato.cms.locator.PropertiesBasedClassListTraversal;
import nz.ac.waikato.cms.locator.example.pkgA.ConcreteClassA;
import nz.ac.waikato.cms.locator.example.pkgA.ConcreteClassB;
import nz.ac.waikato.cms.locator.example.pkgA.InterfaceImplA;
import nz.ac.waikato.cms.locator.example.pkgB.ConcreteClassC;
import nz.ac.waikato.cms.locator.example.pkgB.InterfaceImplB;
import nz.ac.waikato.cms.locator.example.pkgB.InterfaceImplC;

import java.util.Properties;

/**
 * Demonstrates how to initializes the {@link ClassLister} with properties.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ClassListerExamplePropertiesBasedClassList {

  public static void main(String[] args) {
    // NB: in the following the properties objects get constructed by hand, but
    // these could just reside in the class and be loaded from disk as well

    Properties props = new Properties();
    props.setProperty(AbstractAncestor.class.getName(), ConcreteClassA.class.getName() + "," + ConcreteClassB.class.getName() + "," + ConcreteClassC.class.getName());
    props.setProperty(SomeInterface.class.getName(), InterfaceImplA.class.getName() + "," + InterfaceImplB.class.getName() + "," + InterfaceImplC.class.getName());

    // configuring the class hierarchies
    Properties pkgs = new Properties();
    pkgs.put(AbstractAncestor.class.getName(), "nz.ac.waikato.cms.locator.example.pkgA,nz.ac.waikato.cms.locator.example.pkgB");
    pkgs.put(SomeInterface.class.getName(), "nz.ac.waikato.cms.locator.example.pkgA,nz.ac.waikato.cms.locator.example.pkgB");

    // blacklisted classes
    Properties black = new Properties();
    black.put(AbstractAncestor.class.getName(), ".*C");  // anything that ends with a capital "C"
    black.put(SomeInterface.class.getName(), "nz.ac.waikato.cms.locator.example.pkgB.InterfaceImplInternal");  // specific class

    // initialize
    PropertiesBasedClassListTraversal propsBased = new PropertiesBasedClassListTraversal(props);
    ClassLister lister = ClassLister.getSingleton(propsBased);
    lister.setPackages(pkgs);
    lister.setBlacklist(black);
    lister.initialize();

    Class[] classes;
    // abstract class
    System.out.println("\nAbstract super class: " + AbstractAncestor.class.getName());
    classes = lister.getClasses(AbstractAncestor.class);
    for (Class cls: classes)
      System.out.println("- " + cls.getName());
    // interface
    System.out.println("\nInterface: " + SomeInterface.class.getName());
    classes = lister.getClasses(SomeInterface.class);
    for (Class cls: classes)
      System.out.println("- " + cls.getName());
  }
}

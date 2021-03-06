/*
// Licensed to Julian Hyde under one or more contributor license
// agreements. See the NOTICE file distributed with this work for
// additional information regarding copyright ownership.
//
// Julian Hyde licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
*/
package org.eigenbase.util;

import java.util.*;

/**
 * CompoundClosableAllocation represents a collection of ClosableAllocations
 * which share a common lifecycle. It guarantees that allocations are closed in
 * the reverse order in which they were added.
 */
public class CompoundClosableAllocation implements ClosableAllocationOwner {
  //~ Instance fields --------------------------------------------------------

  /**
   * List of owned ClosableAllocation objects.
   */
  protected List<ClosableAllocation> allocations;

  //~ Constructors -----------------------------------------------------------

  public CompoundClosableAllocation() {
    allocations = new LinkedList<ClosableAllocation>();
  }

  //~ Methods ----------------------------------------------------------------

  // implement ClosableAllocationOwner
  public void addAllocation(ClosableAllocation allocation) {
    allocations.add(allocation);
  }

  // implement ClosableAllocation
  public void closeAllocation() {
    // traverse in reverse order
    ListIterator<ClosableAllocation> iter =
        allocations.listIterator(allocations.size());
    while (iter.hasPrevious()) {
      ClosableAllocation allocation = iter.previous();

      // NOTE:  nullify the entry just retrieved so that if allocation
      // calls back to forgetAllocation, it won't find itself
      // (this prevents a ConcurrentModificationException)
      iter.set(null);
      allocation.closeAllocation();
    }
    allocations.clear();
  }

  /**
   * Forgets an allocation without closing it.
   *
   * @param allocation the allocation to forget
   * @return whether the allocation was known
   */
  public boolean forgetAllocation(ClosableAllocation allocation) {
    return allocations.remove(allocation);
  }

  /**
   * @return whether any allocations remain unclosed
   */
  public boolean hasAllocations() {
    return !allocations.isEmpty();
  }
}

// End CompoundClosableAllocation.java

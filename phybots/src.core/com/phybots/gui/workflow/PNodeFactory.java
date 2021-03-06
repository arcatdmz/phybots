/*
 * PROJECT: Phybots at http://phybots.com/
 * ----------------------------------------------------------------------------
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Phybots.
 *
 * The Initial Developer of the Original Code is Jun Kato.
 * Portions created by the Initial Developer are
 * Copyright (C) 2009 Jun Kato. All Rights Reserved.
 *
 * Contributor(s): Jun Kato
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */
package com.phybots.gui.workflow;

import com.phybots.Phybots;
import com.phybots.workflow.Action;
import com.phybots.workflow.ControlNode;
import com.phybots.workflow.Edge;
import com.phybots.workflow.Fork;
import com.phybots.workflow.Join;
import com.phybots.workflow.Node;
import com.phybots.workflow.Transition;

public class PNodeFactory {

	public static PNodeAbstractImpl newNodeInstance(Node node) {
		PNodeAbstractImpl pNodeAbstractImpl;
		if (node instanceof Action) {
			pNodeAbstractImpl = new PActionNode((Action) node);
		} else if (node instanceof ControlNode) {
			pNodeAbstractImpl = new PControlNode((ControlNode) node);
		} else {
			pNodeAbstractImpl = null;
			if (node == null) {
				System.err.println("No node provided.");
			} else {
				System.err.println("Invalid type node: "
						+ node.getClass().getSimpleName());
			}
		}
		return pNodeAbstractImpl;
	}

	public static PLineNodeAbstractImpl newEdgeInstance(Edge edge) {
		PLineNodeAbstractImpl pNodeAbstractImpl;
		if (edge instanceof Transition) {
			pNodeAbstractImpl = new PTransitionLineNode((Transition) edge);
		} else if (edge.getSource() instanceof Fork) {
			pNodeAbstractImpl = new PForkLineNode(edge);
		} else if (edge.getDestination() instanceof Join) {
			pNodeAbstractImpl = new PJoinLineNode(edge);
		} else {
			pNodeAbstractImpl = null;
			Phybots.getInstance().getErrorStream().print("Invalid type edge: ");
			Phybots.getInstance().getErrorStream().println(edge.getClass().getSimpleName());
		}
		return pNodeAbstractImpl;
	}
}

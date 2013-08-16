package hunternif.util;

import java.util.ArrayList;
import java.util.List;

public class Tree<T> {
	public Node<T> root;

	public Tree(T rootData) {
		root = new Node<T>(null, rootData);
	}

	public static class Node<T> {
		public T data;
		public Node<T> parent;
		public List<Node<T>> children;
		
		public Node(Node<T> parent, T data) {
			this.parent = parent;
			this.data = data;
			this.children = new ArrayList<>();
		}
		
		public void addChild(Node<T> child) {
			child.parent = this;
			children.add(child);
		}
		
		public void setParent(Node<T> parent) {
			this.parent = parent;
			if (parent != null) {
				for (Node<T> sibling : parent.children) {
					if (sibling == this) {
						return;
					}
				}
				parent.children.add(this);
			}
		}
		
		public void insertParent(Node<T> midParent) {
			Node<T> upParent = this.parent;
			if (upParent != null) {
				upParent.children.remove(this);
				upParent.addChild(midParent);
			}
			midParent.setParent(upParent);
			midParent.addChild(this);
		}
		
		public void absorbNode(Node<T> node) {
			for (Node<T> child : node.children) {
				addChild(child);
			}
			children.remove(node);
		}
		
		public void sequentializePreOrder(List<T> sequence) {
			sequence.add(data);
			for (Node<T> child : children) {
				child.sequentializePreOrder(sequence);
			}
		}
		
		public void sequentializePostOrder(List<T> sequence) {
			for (Node<T> child : children) {
				child.sequentializePostOrder(sequence);
			}
			sequence.add(data);
		}
		
		@Override
		public String toString() {
			String result = "";
			if (parent == null) {
				result += "root: ";
			}
			result += String.valueOf(data);
			if (!children.isEmpty()) {
				result += children.toString();
			}
			return result;
		}
	}
	
	public List<T> sequentializePostOrder() {
		List<T> sequence = new ArrayList<>(); 
		root.sequentializePostOrder(sequence);
		return sequence;
	}
	
	@Override
	public String toString() {
		return "Tree: " + root.toString();
	}
}

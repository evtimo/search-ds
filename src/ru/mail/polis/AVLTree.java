package ru.mail.polis;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;

public class AVLTree<E extends Comparable<E>> extends AbstractSet<E> implements BalancedSortedSet<E> {

    private final Comparator<E> comparator;

    private Node root = null; //todo: Создайте новый класс если нужно. Добавьте новые поля, если нужно.
    private int size;
    //todo: добавьте дополнительные переменные и/или методы если нужно

    public AVLTree() {
        this(null);
    }

    public AVLTree(Comparator<E> comparator) {
        this.comparator = comparator;
    }

    public class Node {

        E value;
        Node left = null;
        Node right = null;
        int height = 1;

        Node(E value) {
            this.value = value;
            this.left = null;
            this.right = null;
            this.height = 1;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("N{");
            sb.append("d=").append(value);
            if (left != null) {
                sb.append(", l=").append(left);
            }
            if (right != null) {
                sb.append(", r=").append(right);
            }
            sb.append(", h=").append(height);
            sb.append('}');
            return sb.toString();
        }

        int needBalance() {
            int heightLeft = this.left != null ? this.left.height : 0;
            int hieghtRight = this.right != null ? this.right.height : 0;
            return hieghtRight - heightLeft;
        }

        void fixHeight() {
            int heightLeft = this.left != null ? this.left.height : 0;
            int heightRight = this.right != null ? this.right.height : 0;
            this.height = Math.max(heightLeft, heightRight) + 1;
        }
    }

    /**
     * Вставляет элемент в дерево.
     * Инвариант: на вход всегда приходит NotNull объект, который имеет корректный тип
     *
     * @param value элемент который необходимо вставить
     * @return true, если элемент в дереве отсутствовал
     */

    private boolean wasAdd;

    @Override
    public boolean add(E value) {
        if (value == null) {
            throw new NullPointerException("value is null");
        }
        wasAdd = true;
        root = new Object() {
            Node add(Node tmp, E value) {
                if (tmp == null) {
                    return new Node(value);
                }
                int cmp = compare(value, tmp.value);
                if (cmp == 0) wasAdd = false;
                if (cmp < 0) {
                    tmp.left = add(tmp.left, value);
                } else if (cmp > 0) {
                    tmp.right = add(tmp.right, value);
                }
                return makeBalanced(tmp);
            }
        }.add(root, value);
        if (wasAdd) {
            size++;
        }
        return wasAdd;
    }


    private Node rotateRight(Node tmp) {
        Node n = tmp.left;
        tmp.left = n.right;
        n.right = tmp;
        tmp.fixHeight();
        n.fixHeight();
        return n;
    }

    private Node rotateLeft(Node tmp) {
        Node n = tmp.right;
        tmp.right = n.left;
        n.left = tmp;
        tmp.fixHeight();
        n.fixHeight();
        tmp.fixHeight();
        return n;
    }

    private Node makeBalanced(Node tmp) {
        tmp.fixHeight();
        if (tmp.needBalance() == 2) {
            if (tmp.right.needBalance() < 0)
                tmp.right = rotateRight(tmp.right);
            return rotateLeft(tmp);
        }
        if (tmp.needBalance() == -2) {
            if (tmp.left.needBalance() > 0)
                tmp.left = rotateLeft(tmp.left);
            return rotateRight(tmp);
        }
        return tmp;
    }

    /**
     * Удаляет элемент с таким же значением из дерева.
     * Инвариант: на вход всегда приходит NotNull объект, который имеет корректный тип
     *
     * @param object элемент который необходимо вставить
     * @return true, если элемент содержался в дереве
     */

    private boolean wasRemove;

    @Override
    public boolean remove(Object object) {
        @SuppressWarnings("unchecked")
        E value = (E) object;
        if (value == null) {
            throw new NullPointerException("value is null");
        }
        wasRemove = false;
        root = new Object() {
            Node findMin(Node tmp) {
                return tmp.left != null ? findMin(tmp.left) : tmp;
            }

            Node removeMin(Node tmp) {
                if (tmp.left == null) {
                    return tmp.right;
                }
                tmp.left = removeMin(tmp.left);
                return makeBalanced(tmp);
            }

            Node remove(Node tmp, E value) {
                if (tmp == null) {
                    return null;
                }
                int cmp = compare(value, tmp.value);
                if (cmp == 0) {
                    wasRemove = true;
                    if (tmp.left == null) {
                        return tmp.right;
                    } else if (tmp.right == null) {
                        return tmp.left;
                    } else {
                        Node r = tmp;
                        tmp = findMin(r.right);
                        tmp.right = removeMin(r.right);
                        tmp.left = r.left;
                    }
                } else if (cmp < 0) {
                    tmp.left = remove(tmp.left, value);
                } else {
                    tmp.right = remove(tmp.right, value);
                }
                return makeBalanced(tmp);
            }
        }.remove(root, value);
        if (wasRemove) {
            size--;
        }
        return wasRemove;
    }

    /**
     * Ищет элемент с таким же значением в дереве.
     * Инвариант: на вход всегда приходит NotNull объект, который имеет корректный тип
     *
     * @param object элемент который необходимо поискать
     * @return true, если такой элемент содержится в дереве
     */
    @Override
    public boolean contains(Object object) {
        @SuppressWarnings("unchecked")
        E value = (E) object;
        if (value == null) {
            throw new NullPointerException("value is null");
        }
        if (!isEmpty()) {
            Node curr = root;
            while (curr != null) {
                int cmp = compare(curr.value, value);
                if (cmp == 0) {
                    return true;
                } else if (cmp < 0) {
                    curr = curr.right;
                } else if (cmp > 0) {
                    curr = curr.left;
                }
            }
        }
        return false;
    }

    /**
     * Ищет наименьший элемент в дереве
     *
     * @return Возвращает наименьший элемент в дереве
     * @throws NoSuchElementException если дерево пустое
     */
    @Override
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException("first");
        }
        Node curr = root;
        while (curr.left != null) {
            curr = curr.left;
        }
        return (E) curr.value;
    }

    /**
     * Ищет наибольший элемент в дереве
     *
     * @return Возвращает наибольший элемент в дереве
     * @throws NoSuchElementException если дерево пустое
     */
    @Override
    public E last() {
        if (isEmpty()) {
            throw new NoSuchElementException("last");
        }
        Node curr = root;
        while (curr.right != null) {
            curr = curr.right;
        }
        return (E) curr.value;
    }

    private int compare(E v1, E v2) {
        return comparator == null ? v1.compareTo(v2) : comparator.compare(v1, v2);
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public String toString() {
        return "AVLTree{" +
                "tree=" + root +
                "size=" + size + ", " +
                '}';
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        throw new UnsupportedOperationException("subSet");
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        throw new UnsupportedOperationException("headSet");
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        throw new UnsupportedOperationException("tailSet");
    }

    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException("iterator");
    }

    /**
     * Обходит дерево и проверяет что высоты двух поддеревьев
     * различны по высоте не более чем на 1
     *
     * @throws NotBalancedTreeException если высоты отличаются более чем на один
     */
    @Override
    public void checkBalanced() throws NotBalancedTreeException {
        traverseTreeAndCheckBalanced(root);
    }

    private int traverseTreeAndCheckBalanced(Node curr) throws NotBalancedTreeException {
        if (curr == null) {
            return 1;
        }
        int leftHeight = traverseTreeAndCheckBalanced(curr.left);
        int rightHeight = traverseTreeAndCheckBalanced(curr.right);
        if (Math.abs(leftHeight - rightHeight) > 1) {
            throw NotBalancedTreeException.create("The heights of the two child subtrees of any node must be differ by at most one",
                    leftHeight, rightHeight, curr.toString());
        }
        return Math.max(leftHeight, rightHeight) + 1;
    }

    public static void main(String[] args) throws NotBalancedTreeException {
        AVLTree AVL = new AVLTree<Integer>();
    }

}

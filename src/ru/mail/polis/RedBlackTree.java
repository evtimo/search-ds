package ru.mail.polis;

import java.util.*;

public class RedBlackTree<E extends Comparable<E>> extends AbstractSet<E> implements BalancedSortedSet<E> {

    private static final Random RANDOM = new Random();
    private final Comparator<E> comparator;
    private Node nil = new Node(Color.BLACK, null, null, null, null);
    private Node root = new Node(Color.BLACK, null, nil, nil, nil);
    private int size;
    //todo: добавьте дополнительные переменные и/или методы если нужно

    public RedBlackTree() {
        this(null);
    }

    public RedBlackTree(Comparator<E> comparator) {
        this.comparator = comparator;
    }

    /**
     * Вставляет элемент в дерево.
     * Инвариант: на вход всегда приходит NotNull объект, который имеет корректный тип
     * <p>
     * param value элемент который необходимо вставить
     *
     * @return true, если элемент в дереве отсутствовал
     */

    private void insertFixup(Node tmp) {
        Node uncle;
        boolean flag; //Отвечает, левым(0) или правым(1) потомком является узел
        while (tmp.parent.color == Color.RED) {
            if (tmp.parent == tmp.parent.parent.left) {
                uncle = tmp.parent.parent.right;
                flag = true;
            } else {
                uncle = tmp.parent.parent.left;
                flag = false;
            }
            //Если цвет дяди - красный
            if (uncle.color == Color.RED) {
                tmp.parent.color = Color.BLACK;//Отца - черным
                uncle.color = Color.BLACK;//Дядю - черным
                tmp.parent.parent.color = Color.RED;//Деда - красным
                tmp = tmp.parent.parent;
            } else {
                Node X;
                if (flag) {
                    X = tmp.parent.right;
                } else {
                    X = tmp.parent.left;
                }
                if (tmp == X) {
                    tmp = tmp.parent;
                    if (flag) rotateLeft(tmp);
                    else rotateRight(tmp);
                }
                tmp.parent.color = Color.BLACK;
                tmp.parent.parent.color = Color.RED;
                if (!flag) rotateLeft(tmp.parent.parent);
                else rotateRight(tmp.parent.parent);
            }
        }

        root.color = Color.BLACK;
    }


    private void rotateRight(Node tmp) {
        Node x = tmp.left;
        tmp.left = x.right;

        if (x.right != nil) {
            x.right.parent = tmp;
        }
        x.parent = tmp.parent;
        if (tmp.parent == nil) {
            root = x;
        } else if (tmp == tmp.parent.right) {
            tmp.parent.right = x;
        } else {
            tmp.parent.left = x;
        }
        x.right = tmp;
        tmp.parent = x;
    }

    private void rotateLeft(Node tmp) {
        Node x = tmp.right;
        tmp.right = x.left;

        if (x.left != nil) {
            x.left.parent = tmp;
        }
        x.parent = tmp.parent;
        if (tmp.parent == nil) {
            root = x;
        } else if (tmp == tmp.parent.left) {
            tmp.parent.left = x;
        } else {
            tmp.parent.right = x;
        }
        x.left = tmp;
        tmp.parent = x;
    }


    @Override
    public boolean add(E value) {
        if (value == null) {
            throw new NullPointerException("Value is null");
        }
        if (contains(value)) return false;
        if (root.value == null) {
            root = new Node(Color.BLACK, value, nil, nil, nil);
            size++;
            return true;
        }

        Node z;
        Node x = nil;
        Node tmp = root;

        //Идём от корня до тех пор, пока указатель не станет nil
        while (tmp != nil) {
            x = tmp;
            if (compare(value, (E) tmp.value) > 0) {
                tmp = tmp.right;
            } else {
                tmp = tmp.left;
            }
        }

        //Вставляем вместо него новый элемент с nil-потомками и красным цветом
        if (compare(value, (E) x.value) < 0) {
            z = new Node(Color.RED, value, x, nil, nil);
            x.left = z;
        } else {
            z = new Node(Color.RED, value, x, nil, nil);
            x.right = z;
        }

        //Проверяем балансировку
        insertFixup(z);
        size++;
        return true;

    }

    private Node<E> search(Node tmp, E value) {
        if (tmp == nil || value == tmp.value) {
            return tmp;
        }
        if (compare(value, (E) tmp.value) < 0) {
            return search(tmp.left, value);
        } else
            return search(tmp.right, value);
    }

    /**
     * Удаляет элемент с таким же значением из дерева.
     * Инвариант: на вход всегда приходит NotNull объект, который имеет корректный тип
     *
     * @param object элемент который необходимо вставить
     * @return true, если элемент содержался в дереве
     */
    @Override
    public boolean remove(Object object) {
        @SuppressWarnings("unchecked")
        E value = (E) object;

        Node balance, next; //Удаленная вершина, от которой нужно будет балансировать
        Node rm = search(root, value);
        boolean wasBlackRemoved = false;
        if (rm == nil) return false;
        if (rm.color == Color.BLACK) wasBlackRemoved = true;

        //Если имеет только одного ребенка, присоединяем ребенка к деду
        if (rm.left == nil) {
            balance = rm.right;
            changeRelationBetween(rm, rm.right);
        } else if (rm.right == nil) {
            balance = rm.left;
            changeRelationBetween(rm, rm.left);
        } else {
            //Если имеет обоих детей, то находим вершину со следующим значеним ключа
            next = findNext(rm.right); //Ищем по правому поддереву
            if (next.color == Color.BLACK) wasBlackRemoved = true;
            balance = next.right;
            //Если следующее значение является ребенком удаляемого
            if (next.parent == rm) {
                balance.parent = next;
            } else {
                changeRelationBetween(next, next.right);
                next.right = rm.right;
                rm.right.parent = rm;
            }
            changeRelationBetween(rm, next);
            next.left = rm.left;
            next.left.parent = next;
            next.color = rm.color;
        }
        //Только если был удален черный -
        if (wasBlackRemoved) {
            removeFixup(balance);
        }
        size--;
        return true;
    }

    private Node findNext(Node tmp) {
        Node curr = tmp;
        while (curr.right != nil) {
            curr = curr.right;
        }
        return curr;
    }

    private void changeRelationBetween(Node rm, Node rm_child) {
        if (rm.parent == nil) {
            root = rm_child;
        } else if (rm == rm.parent.left) {
            rm.parent.left = rm_child;
        } else {
            rm.parent.left = rm_child;
        }
        rm_child.parent = rm.parent;
    }

    private void removeFixup(Node tmp) {
        while (tmp != root && tmp.color != Color.RED) {
            Node sibling;
            boolean flag;
            if (tmp == tmp.parent.left) {
                sibling = tmp.parent.right;
                flag = true;
            } else {
                sibling = tmp.parent.right;
                flag = false;
            }
            if (sibling.color == Color.RED) {
                sibling.color = Color.BLACK;
                tmp.parent.color = Color.RED;
                rotateLeft(tmp.parent);
                if (flag) sibling = tmp.parent.right;
                else sibling = tmp.parent.left;
            }
            if (sibling.left.color != Color.RED && sibling.right.color != Color.RED) {
                sibling.color = Color.RED;
                tmp = tmp.parent;
            } else {
                Node wl, wr;
                wr = flag ? sibling.right : sibling.left;
                wl = flag ? sibling.right : sibling.left;
                if (wr.color == Color.BLACK) {
                    wl.color = Color.BLACK;
                    sibling.color = Color.RED;
                    if (flag) rotateRight(sibling);
                    else rotateLeft(sibling);
                    sibling = tmp.parent.right;
                }
                sibling.color = tmp.parent.color;
                tmp.parent.color = Color.BLACK;
                wr.color = Color.BLACK;
                if (flag) rotateLeft(tmp.parent);
                else rotateRight(tmp.parent);
                tmp = root;
            }
        }

        tmp.color = Color.BLACK;
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
            throw new NullPointerException("Null value!");
        }
        if (!this.isEmpty()) {
            Node curr = root;
            while (curr != nil) {
                int flag = compare(value, (E) curr.value);
                if (flag == 0) {
                    return true;
                }
                if (flag < 0) {
                    curr = curr.left;
                }
                if (flag > 0) {
                    curr = curr.right;
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
        if (this.isEmpty()) {
            throw new NoSuchElementException("first");
        }
        Node tmp = root;
        if (tmp.left != nil)
            while (tmp.left != nil) {
                tmp = tmp.left;
            }
        return (E) tmp.value;
    }

    /**
     * Ищет наибольший элемент в дереве
     *
     * @return Возвращает наибольший элемент в дереве
     * @throws NoSuchElementException если дерево пустое
     */
    @Override
    public E last() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("last");
        }
        Node tmp = root;
        if (tmp.right != nil)
            while (tmp.right != nil) {
                tmp = tmp.right;
            }
        return (E) tmp.value;
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
        return this.size;
    }

    @Override
    public String toString() {
        return "RBTree{" +
                "size=" + size + ", " +
                "tree=" + root +
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
     * Обходит дерево и проверяет выполнение свойств сбалансированного красно-чёрного дерева
     * <p>
     * 1) Корень всегда чёрный.
     * 2) Если узел красный, то его потомки должны быть чёрными (обратное не всегда верно)
     * 3) Все пути от узла до листьев содержат одинаковое количество чёрных узлов (чёрная высота)
     *
     * @throws NotBalancedTreeException если какое-либо свойство невыполнено
     */
    @Override
    public void checkBalanced() throws NotBalancedTreeException {
        if (root != null) {
            if (root.color != Color.BLACK) {
                throw new NotBalancedTreeException("Root must be black");
            }
            traverseTreeAndCheckBalanced(root);
        }
    }

    private int traverseTreeAndCheckBalanced(Node node) throws NotBalancedTreeException {
        if (node == null) {
            return 1;
        }
        int leftBlackHeight = traverseTreeAndCheckBalanced(node.left);
        int rightBlackHeight = traverseTreeAndCheckBalanced(node.right);
        if (leftBlackHeight != rightBlackHeight) {
            throw NotBalancedTreeException.create("Black height must be equal.", leftBlackHeight, rightBlackHeight, node.toString());
        }
        if (node.color == Color.RED) {
            checkRedNodeRule(node);
            return leftBlackHeight;
        }
        return leftBlackHeight + 1;
    }

    private void checkRedNodeRule(Node node) throws NotBalancedTreeException {
        if (node.left != null && node.left.color != Color.BLACK) {
            throw new NotBalancedTreeException("If a node is red, then left child must be black.\n" + node.toString());
        }
        if (node.right != null && node.right.color != Color.BLACK) {
            throw new NotBalancedTreeException("If a node is red, then right child must be black.\n" + node.toString());
        }
    }

    enum Color {
        RED, BLACK
    }

    static final class Node<E> {
        E value;
        Node<E> left;
        Node<E> right;
        Node<E> parent;
        Color color = Color.BLACK;

        @Override
        public String toString() {
            return "Node{" +
                    "value=" + value +
                    ", left=" + left +
                    ", right=" + right +
                    //    ", parent=" + parent +
                    ", color=" + color +
                    "} \n";
        }

        Node(Color color, E value, Node<E> parent, Node<E> right, Node<E> left) {
            this.color = color;
            this.value = value;
            this.parent = parent;
            this.right = right;
            this.left = left;
        }

        Node(Node N) {
            color = N.color;
            value = (E) N.value;
            parent = N.parent;
            right = N.right;
            left = N.left;
        }

    }

    public static void main(String[] args) {
        RedBlackTree RB = new RedBlackTree();

        for (int i = 0; i < 1000; i++) {
            RB.add(RANDOM.nextInt(1000));
        }
        System.out.println(RB.toString());

        for (int i = 0; i < 0; i++) {
            RB.remove(RANDOM.nextInt(1000));
        }
        System.out.println(RB.toString());

    }

}

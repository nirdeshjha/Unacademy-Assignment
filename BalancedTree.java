import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class BalancedTree {

  private TreeNode root;
  private int totalNodes;
  private final Object lock;

  private Map<String, Double> memberScores;

  public BalancedTree() {
    root = null;
    lock = new Object();

    memberScores = new ConcurrentHashMap<>();
    totalNodes = 0;
  }

  private void inorder(TreeNode root) {
    if (root == null)
      return;
    inorder(root.getLft());
    System.out.println(root);
    inorder(root.getRgt());
  }

  private void preOrder(TreeNode root) {
    if (root == null)
      return;
    System.out.println(root);
    preOrder(root.getLft());
    preOrder(root.getRgt());
  }

  private void printTree(TreeNode root) {
    System.out.println("\nInorder: ");
    inorder(root);
    System.out.println("\nPreorder: ");
    preOrder(root);
    System.out.println();
  }

  TreeNode rotateLeft(TreeNode root) {
    TreeNode x = root;
    TreeNode y = root.getRgt();

    x.setRgt(y.getLft());
    y.setLft(x);

    x = manageNode(x);
    y = manageNode(y);

    return y;
  }

  TreeNode rotateRight(TreeNode root) {
    TreeNode x = root;
    TreeNode y = x.getLft();

    x.setLft(y.getRgt());
    y.setRgt(x);

    x = manageNode(x);
    y = manageNode(y);

    return y;
  }

  TreeNode balanceTree(TreeNode root) {
    if (Math.abs(root.getBalance()) <= 1) {
      // the tree is already balanced;
      return root;
    }
    if (root.getBalance() == -2) {
      // left subtree size is larger
      if (root.getLft().getBalance() != -1) {
        // rotate the left subtree to make it's balance negative too
        root.setLft(rotateLeft(root.getLft()));
      }
      return rotateRight(root);
    }
    if (root.getBalance() == 2) {
      // right subtree size is larger
      if (root.getRgt().getBalance() != 1) {
        root.setRgt(rotateRight(root.getRgt()));
      }
      return rotateLeft(root);
    }

    assert (false);
    return null;
  }

  TreeNode manageNode(TreeNode root) {
    int lftHeight = root.getLft() == null ? 0 : root.getLft().getHeight();
    int rgtHeight = root.getRgt() == null ? 0 : root.getRgt().getHeight();

    int lftSize = root.getLft() == null ? 0 : root.getLft().getSize();
    int rgtSize = root.getRgt() == null ? 0 : root.getRgt().getSize();

    root.setHeight(Math.max(lftHeight, rgtHeight) + 1);
    root.setSize(lftSize + rgtSize + 1);
    root.setBalance(rgtHeight - lftHeight);

    if (root.getLft() != null)
      root.getLft().setPar(root);
    if (root.getRgt() != null)
      root.getRgt().setPar(root);

    return root;
  }

  public TreeNode insert(TreeNode root, TreeNode newNode) {
    if (root == null) {
      return manageNode(newNode);
    }
    if (root.compareTo(newNode) == 0) {
      // Already present in the tree. No need to insert
      assert (false);
      return null;
    }
    if (root.compareTo(newNode) == 1) {
      // key to insert is smaller and hence it must be inserted in left subtree
      root.setLft(insert(root.getLft(), newNode));
    } else {
      // key to insert is larger and hence it must be inserted in right subtree
      root.setRgt(insert(root.getRgt(), newNode));
    }
    root = manageNode(root);
    return balanceTree(root);
  }

  public int insert(double score, String member) {
    int insertCount = 0;

    // check if the value is already present in the tree
    if (memberScores.containsKey(member)) {
      return insertCount;
    }
    // insert it in hashmap
    memberScores.put(member, score);
    // insert it in tree
    synchronized (lock) {
      insertCount++;
      TreeNode newNode = new TreeNode(score, member);

      root = insert(root, newNode);
      root.setPar(null);

      // printTree(root);

      totalNodes++;
    }

    return insertCount;
  }

  public int rank(TreeNode root, TreeNode node) {
    if (root == null) {
      // as this can't be the case as our HashMap ensures this
      assert (false);
      return -1;
    }
    if (root.compareTo(node) == 0) {
      return (root.getLft() == null ? 0 : root.getLft().getSize());
    }
    if (root.compareTo(node) == 1) {
      // root is greater
      return rank(root.getLft(), node);
    }
    if (root.compareTo(node) == -1) {
      int cntLft = root.getLft() == null ? 0 : root.getLft().getSize();
      return rank(root.getRgt(), node) + cntLft + 1;
    }
    // flow never comes here..
    assert (false);
    return -1;
  }

  public int rank(String member) {
    // check whether the member is a valid member or not
    if (!memberScores.containsKey(member))
      return -1;
    // find the rank
    synchronized (lock) {
      double score = memberScores.get(member);
      TreeNode curNode = new TreeNode(score, member);
      return rank(root, curNode);
    }
  }

  public void lowerBound(TreeNode root, int l, ArrayList<TreeNode> list) {
    if (root == null)
      return;
    list.add(root);
    int cntLft = root.getLft() == null ? 0 : root.getLft().getSize();
    if (cntLft == l) {
      // this is the key we are looking for
      return;
    }
    if (l < cntLft) {
      lowerBound(root.getLft(), l, list);
      return;
    }
    lowerBound(root.getRgt(), l - cntLft - 1, list);
  }

  public void traverse(TreeNode root, ArrayList<TreeNode> list) {
    if (root == null)
      return;

    traverse(root.getLft(), list);
    list.add(root);
    traverse(root.getRgt(), list);
  }

  private int convertIndex(int ind) {
    int x = ind / totalNodes;
    if (x < 0)
      ind += (-x) * totalNodes;
    if (ind < 0)
      ind += totalNodes;
    return ind;
  }

  public ArrayList<String> getInRange(int l, int r, boolean withScores) {
    l = convertIndex(l);
    r = convertIndex(r);

    ArrayList<TreeNode> lft = new ArrayList<TreeNode>();
    ArrayList<TreeNode> rgt = new ArrayList<TreeNode>();

    ArrayList<String> ans = new ArrayList<String>();

    synchronized (lock) {
      lowerBound(root, l, lft);
      lowerBound(root, r, rgt);

      // find the lowest common acestor
      int lcaIndex = -1;
      int n1 = lft.size(), n2 = rgt.size();
      int i = 0, j = 0;
      // O(logN)
      while (i < n1 && j < n2) {
        if (lft.get(i).equals(rgt.get(j))) {
          i++;
          j++;
          continue;
        }
        // the node previous to this is the required LCA
        lcaIndex = i - 1;
        break;
      }
      if (lcaIndex == -1) {
        // LCA is one of the mentioned node itself
        lcaIndex = Math.min(n1, n2) - 1;
      }

      for (int ind = n1 - 1; ind > lcaIndex;) {
        // add this node
        TreeNode curNode = lft.get(ind);
        ans.add(curNode.getMember());
        if (withScores)
          ans.add(curNode.getScore());

        // add all it's right subtree nodes
        ArrayList<TreeNode> list = new ArrayList<TreeNode>();
        traverse(curNode.getRgt(), list);
        for (TreeNode node : list) {
          ans.add(node.getMember());
          if (withScores) {
            ans.add(node.getScore());
          }
        }

        // go to it's parent
        if (curNode.getPar().getLft().equals(curNode)) {
          // it is the left child of it's parent
          ind--;
          continue;
        } else {
          // right child of parent
          while (ind > lcaIndex) {
            TreeNode cur = lft.get(ind);
            if (cur.getPar().getRgt().equals(cur)) {
              // if this node is the right child of it's parent
              ind--;
              continue;
            }
            ind--;
            break;
          }
        }
      }

      // add LCA
      TreeNode lca = lft.get(lcaIndex);
      ans.add(lca.getMember());
      if (withScores)
        ans.add(lca.getScore());

      // process the path from [LCA --> r_bound]
      ArrayList<TreeNode> included = new ArrayList<TreeNode>();
      for (int ind = n2 - 1; ind > lcaIndex;) {
        TreeNode curNode = rgt.get(ind);
        included.add(curNode);
        if (curNode.getPar().getRgt().equals(curNode)) {
          // right child of it's parent
          ind--;
          continue;
        }
        while (ind > lcaIndex) {
          if (curNode.getPar().getLft().equals(curNode)) {
            // left child of it's parent
            ind--;
            continue;
          }
          ind--;
          break;
        }
      }

      // add all the nodes and their left subtrees in the reverse order
      int len = included.size();
      for (int ind = len - 1; ind >= 0; ind--) {
        TreeNode curNode = included.get(ind);

        // add all it's left subtree nodes
        ArrayList<TreeNode> list = new ArrayList<TreeNode>();
        traverse(curNode.getLft(), list);
        for (TreeNode node : list) {
          ans.add(node.getMember());
          if (withScores) {
            ans.add(node.getScore());
          }
        }

        // add this node
        ans.add(curNode.getMember());
        if (withScores)
          ans.add(curNode.getScore());
      }

    }
    return ans;
  }

  public int getTotalNodes() {
    return totalNodes;
  }

  public void setTotalNodes(int totalNodes) {
    this.totalNodes = totalNodes;
  }
}
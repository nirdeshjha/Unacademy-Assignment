
public class TreeNode implements Comparable<TreeNode> {
  private TreeNode lft;
  private TreeNode rgt;
  private TreeNode par;

  private final Double score;
  private final String member;

  private int size;
  private int height;
  private int balance;

  public TreeNode(double score, String member) {
    this.score = score;
    this.member = member;

    size = 0;
    height = 0;
    balance = 0;

    lft = null;
    rgt = null;
    par = null;
  }

  @Override
  public int compareTo(TreeNode other) {
    if (other.score > score)
      return -1;
    if (other.score < score)
      return 1;

    int result = member.compareTo(other.member);
    if (result < 0)
      return -1;
    if (result > 0)
      return 1;
    return 0;
  }

  @Override
  public String toString() {
    String result = "<" + score + ", " + member + ">";
    result += (par == null ? " PARENT (- -)" : "PARENT (" + par.score + ", " + par.member + ")");
    result += (lft == null ? " LEFT: (-) " : " LEFT: (" + lft.score + ", " + lft.member + ") ");
    result += (rgt == null ? " RIGT: (-) " : " RIGT: (" + rgt.score + ", " + rgt.member + ") ");
    result += " SIZE: " + size;
    result += " HEIGHT: " + height;
    result += " BALANCE: " + balance;
    return result;
  }

  public TreeNode getLft() {
    return lft;
  }

  public void setLft(TreeNode lft) {
    this.lft = lft;
  }

  public TreeNode getRgt() {
    return rgt;
  }

  public void setRgt(TreeNode rgt) {
    this.rgt = rgt;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getBalance() {
    return balance;
  }

  public String getScore() {
    return score.toString();
  }

  public String getMember() {
    return member;
  }

  public void setBalance(int balance) {
    this.balance = balance;
  }

  public TreeNode getPar() {
    return par;
  }

  public void setPar(TreeNode par) {
    this.par = par;
  }

  // @Override
  // public boolean equals(Object obj) {
  // if (obj instanceof TreeNode) {
  // TreeNode other = (TreeNode) obj;
  // return (other.score == this.score && other.member == this.member);
  // }
  // return false;
  // }

}
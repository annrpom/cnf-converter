import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

class NoneTypeE extends Exception {}

abstract class CNF {
    // methods here later
    abstract CNF convert ();
    abstract String toSAT ();
    abstract CNF simplify ();
}

class Atomic extends CNF {
    private char val = ' ';
    private boolean isTrue;

    Atomic (boolean b, String flag) {
        this.isTrue = b;
    }

    Atomic (char val) {
        this.val = val;
    }

    @Override
    public String toString () {
        if (val == ' ') {
            return isTrue ? "TRUE" : "FALSE";
        } else {
            return "\'" + val + "\'";
        }
    }

    @Override
    CNF convert () {
        return this;
    }

    @Override
    String toSAT() {
        if (val == ' ') {
            return isTrue ? "TRUE" : "FALSE";
        } else {
            return String.valueOf(val);
        }
    }

    @Override
    CNF simplify() {
        // cannot simplify atomic
        return this;
    }

    public char getVal() {
        return val;
    }

    public void setVal(char val) {
        this.val = val;
    }

    public boolean isTrue() {
        return isTrue;
    }

    public void setBool(boolean aTrue) {
        this.isTrue = aTrue;
    }
}

class Not extends CNF {
    private CNF value;

    Not (CNF value) {
        this.value = value;
    }

    @Override
    public String toString () {
        return "NOT(" + value + ")";
    }

    @Override
    CNF convert() {
        return new Not(value.convert());
    }

    @Override
    String toSAT() {
        return "¬" + "(" + value.toSAT() + ")";
    }

    @Override
    CNF simplify() {
        CNF nval;
        if (value instanceof Not) {
            nval = ((Not) value).getValue().simplify();
        } else if (value instanceof And) {
            ArrayList<CNF> ors = new ArrayList<>();
            for (CNF o : ((And) value).getPhis()) {
                CNF negapp = new Not(o).simplify();
                ors.add(negapp);
            }
            nval = new Or(ors);
        } else if (value instanceof Or) {
            ArrayList<CNF> and = new ArrayList<>();
            for (CNF o : ((Or) value).getPhis()) {
                CNF negapp = new Not(o).simplify();
                and.add(negapp);
            }
            nval = new And(and);
        } else if (value instanceof Atomic) {
            if (((Atomic) value).getVal() == ' ') {
               if (((Atomic) value).isTrue()) {
                  nval = new Atomic(false, "");
               } else {
                   nval = new Atomic(true, "");
               }
            } else {
                nval = this;
            }
        } else {
            nval = this;
        }
        return nval;
    }

    public CNF getValue() {
        return value;
    }

    public void setValue(CNF value) {
        this.value = value;
    }
}


class Imp extends CNF {
    private CNF left;
    private CNF right;

    Imp (CNF left, CNF right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString () {
        return "IMP(" + left + "," + right + ")";
    }

    @Override
    CNF convert() {
        ArrayList<CNF> ors = new ArrayList<>(Arrays.asList(new Not(left.convert()), right.convert()));
        return new Or(ors);
    }

    @Override
    String toSAT() {
        return "(" + left.toSAT() + ")" + "→" + "(" + right.toSAT() + ")";
    }

    @Override
    CNF simplify() {
        // cannot simply if
        return this;
    }

    public CNF getLeft() {
        return left;
    }

    public void setLeft(CNF left) {
        this.left = left;
    }

    public CNF getRight() {
        return right;
    }

    public void setRight(CNF right) {
        this.right = right;
    }
}


class Iff extends CNF {
    private CNF left;
    private CNF right;
    // same as its recent parent

    Iff (CNF left, CNF right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString () {
        return "IFF(" + left + "," + right + ")";
    }

    @Override
    CNF convert() {
        Imp nleft = new Imp(left, right);
        Imp nright = new Imp(right, left);
        ArrayList<CNF> ands = new ArrayList<>(Arrays.asList(nleft.convert(), nright.convert()));
       return new And(ands);
    }

    @Override
    String toSAT() {
        return "(" + left.toSAT() + ")" + "↔" + "(" + right.toSAT() + ")";
    }

    @Override
    CNF simplify() {
        // cannot simply if
        return null;
    }

    public CNF getLeft() {
        return left;
    }

    public void setLeft(CNF left) {
        this.left = left;
    }

    public CNF getRight() {
        return right;
    }

    public void setRight(CNF right) {
        this.right = right;
    }
}

class Or extends CNF {
    private ArrayList<CNF> phis;

    Or (ArrayList<CNF> phis) {
        this.phis = phis;
    }

    @Override
    public String toString () {
        String returned = "OR(";
        String sep = ",";
        for (int i=0; i<phis.size(); i++) {
            returned += (i == phis.size()-1) ? phis.get(i) + ")": phis.get(i) + sep;
        }
        return phis.size() == 0 ? returned + ")" : returned;
    }

    @Override
    CNF convert() {
        ArrayList<CNF> converted = new ArrayList<>();
        for (CNF phi : phis) {
            converted.add(phi.convert());
        }
        return phis.size() == 0 ? new Atomic(false, "") : new Or(converted);
    }

    @Override
    String toSAT() {
        return phis.size() == 0 ? "FALSE" : phis.stream().map((x) -> "(" + x.toSAT() + ")").collect(Collectors.joining("∨"));
    }

    @Override
    CNF simplify() {
        ArrayList<CNF> simps = new ArrayList<>();
        for (CNF phi : phis) {
            CNF simp = phi.simplify();
            if (simp instanceof Atomic) {
                if (((Atomic) simp).getVal() == ' ') {
                    if (((Atomic) simp).isTrue()) {
                        return new Atomic(true, "");
                    } else if (!((Atomic) simp).isTrue()) {
                        continue;
                    }
                }
            }
            simps.add(simp);
        }
        return simps.size() == 0 ? new Atomic(false, "") : new Or(simps);
    }

    public ArrayList<CNF> getPhis() {
        return phis;
    }

    public void setPhis(ArrayList<CNF> phis) {
        this.phis = phis;
    }
}


class And extends CNF {
    private ArrayList<CNF> phis;

    And (ArrayList<CNF> phis) {
        this.phis = phis;
    }

    @Override
    public String toString () {
        String returned = "AND(";
        String sep = ",";
        for (int i=0; i<phis.size(); i++) {
            returned += (i == phis.size()-1) ? phis.get(i) + ")": phis.get(i) + sep;
        }
        return phis.size() == 0 ? returned + ")" : returned;
    }

    @Override
    CNF convert() {
        ArrayList<CNF> converted = new ArrayList<>();
        for (CNF phi : phis) {
            converted.add(phi.convert());
        }
        return phis.size() == 0 ? new Atomic(true, "") : new And(converted);
    }

    @Override
    String toSAT() {
        return phis.size() == 0 ? "TRUE" : phis.stream().map((x) -> "(" + x.toSAT() + ")").collect(Collectors.joining("∧"));
    }

    @Override
    CNF simplify() {
        ArrayList<CNF> simps = new ArrayList<>();
        for (CNF phi : phis) {
            CNF simp = phi.simplify();
            if (simp instanceof Atomic) {
                if (((Atomic) simp).getVal() == ' ') {
                    if (((Atomic) simp).isTrue()) {
                        continue;
                    } else if (!((Atomic) simp).isTrue()) {
                        return new Atomic(false, "");
                    }
                }
            }
            simps.add(simp);
        }
        return simps.size() == 0 ? new Atomic(true, "") : new And(simps);
    }

    public ArrayList<CNF> getPhis() {
        return phis;
    }

    public void setPhis(ArrayList<CNF> phis) {
        this.phis = phis;
    }
}

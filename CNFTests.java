import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class CNFTests {
    CNF a1, a2, neg, imp, iff, and, or;
    CNF c1, c2, c3, c4, c5, t, f;
    @BeforeEach
    public void CNFCreation() throws NoneTypeE {
        char atom1 = 'p';
        char atom2 = 'q';
        a1 = new Atomic(atom1);
        a2 = new Atomic(atom2);
        neg = new Not(a1);
        imp = new Imp(a1, a2);
        iff = new Iff(a2, a1);
        and = new And(new ArrayList<>(Arrays.asList(a1, a2)));
        or = new Or(new ArrayList<>(Arrays.asList(a2, a1)));


        c1 = Main.toClass(Main.stringTo("NOT(IMP('w','r'))")).get(0);
        c2 = Main.toClass(Main.stringTo("NOT(NOT('q'))")).get(0);
        c3 = Main.toClass(Main.stringTo("OR('c',TRUE)")).get(0);
        c4 = Main.toClass(Main.stringTo("AND('r',FALSE)")).get(0);
        c5 = Main.toClass(Main.stringTo("IMP('w',IFF('q',AND('o',OR('v',NOT(FALSE)))))")).get(0);
        t = new Atomic(true, "");
        f = new Atomic(false, "");
    }

    @Test
    public void Negation() {
        Assertions.assertEquals("FALSE", new Not(t).convert().simplify().toString());
        Assertions.assertEquals("TRUE", new Not(f).convert().simplify().toString());
        Assertions.assertEquals("AND('w',NOT('r'))", c1.convert().simplify().toString());
        Assertions.assertEquals("'q'", c2.convert().simplify().toString());
    }

    static String apply (CNF obj) {
        return obj.convert().simplify().toString();
    }

    @Test
    public void Implication() {
        Assertions.assertEquals("OR(NOT('w'),AND(OR(NOT('q'),AND('o')),OR(OR(NOT('o'),AND(NOT('v'),FALSE)),'q')))", apply(c5));
        Assertions.assertEquals("FALSE", apply(new Imp(t, f)));
        Assertions.assertEquals("TRUE", apply(new Imp(a1, t)));
        Assertions.assertEquals("TRUE", apply(new Imp(f, a1)));
    }

    @Test
    public void Biconditional() {
        Assertions.assertEquals("TRUE", apply(new Iff(t,t)));
        Assertions.assertEquals("TRUE", apply(new Iff(f,f)));
        Assertions.assertEquals("FALSE", apply(new Iff(f, t)));
    }

    @Test
    public void Or() {
        Assertions.assertEquals("TRUE", apply(new Or(new ArrayList<>(Arrays.asList(c2, c4, t, c5, f)))));
        Assertions.assertEquals("FALSE", apply(new Or(new ArrayList<>(Arrays.asList()))));
        Assertions.assertEquals("TRUE", apply(c3));

    }

    @Test
    public void And() {
        Assertions.assertEquals("FALSE", apply(new And(new ArrayList<>(Arrays.asList(c2, c4, t, c5, f)))));
        Assertions.assertEquals("TRUE", apply(new And(new ArrayList<>(Arrays.asList()))));
        Assertions.assertEquals("FALSE", apply(c4));
    }
}

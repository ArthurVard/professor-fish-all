-- (C) 2010, Ralf Laemmel

-- Use cata- and paramorphisms for factorial and other implementations.

data Nat = Zero | Succ { pred :: Nat }

cata :: r -> (r -> r) -> Nat -> r
cata z s Zero = z
cata z s (Succ p) = s (cata z s p)

para :: r -> (Nat -> r -> r) -> Nat -> r
para z s Zero = z
para z s (Succ p) = s p (para z s p)

toInt = cata 0 (+1)
add m = cata m Succ
times m = cata Zero (add m)
factorial = para (Succ Zero) (times . Succ)

main = do
 putStr "5! = "
 let
  c0 = Zero
  c1 = Succ c0
  c2 = Succ c1
  c3 = Succ c2
  c4 = Succ c3
  c5 = Succ c4
 print $ toInt (factorial c5)
 

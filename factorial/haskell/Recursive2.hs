-- (C) 2010, Ralf Laemmel

-- Define factorial and other operations on top of iszero, pred, succ.

import Prelude hiding (succ, pred)

succ = (+1)
pred = (+) (-1)
isZero = (==0)
add n m = if isZero n then m else succ (add (pred n) m)
times n m = if isZero n then 0 else add m (times (pred n) m)
factorial 0 = 1
factorial n = times n (factorial (pred n))

main = do
 putStr "5! = "
 print $ factorial 5

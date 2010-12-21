-- (C) 2010, Ralf Laemmel

-- Implement factorial by enumeration and product.

factorial n = product (enumFromTo 1 n)

main = do
 putStr "5! = "
 print $ factorial 5

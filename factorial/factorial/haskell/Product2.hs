-- (C) 2010, Ralf Laemmel

-- Point-free variation on Product.hs

factorial = product . enumFromTo 1

main = do
 putStr "5! = "
 print $ factorial 5

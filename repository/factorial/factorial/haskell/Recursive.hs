-- (C) 2010, Ralf Laemmel

-- Trivial recursive implementation of factorial

factorial 0 = 1
factorial n = n * factorial (n-1)

main = do
 putStr "5! = "
 print $ factorial 5

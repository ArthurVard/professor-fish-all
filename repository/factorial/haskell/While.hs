-- (C) 2010, Ralf Laemmel

-- Compute factorial in an imperative manner.

import Data.IORef
import Control.Pascal

factorial n
 = do
      r <- newIORef 1
      i <- newIORef n
      while i (>0) (
        readIORef i >>= 
        modifyIORef r . (*) >>
        modifyIORef i ((+) (-1))
       )
      readIORef r

main = do
 putStr "5! = "
 factorial 5 >>= print

-- (C) 2010, Ralf Laemmel

-- We provide an imperative loop constructs with interaction with IORefs.

module Control.Pascal where

import Data.IORef

while :: IORef a -> (a -> Bool) -> IO () -> IO ()
while ref pred body = while'
 where
  while' = do
              v <- readIORef ref
              if (pred v)
                then body >> while'
                else return ()


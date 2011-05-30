module Main where

import Database.HDBC 
import Database.HDBC.Sqlite3

import Total
import Cut

main = do
    let dbName = "test.db" 
    oldTotal <- total "test.db" "meganalysis"
    print oldTotal
    cut "test.db" "meganalysis"
    newTotal <- total "test.db" "meganalysis"
    print newTotal
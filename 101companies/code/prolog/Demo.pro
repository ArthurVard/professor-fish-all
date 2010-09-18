:- ['Total.pro']. 
:- ['Cut.pro']. 
:- ['Depth.pro']. 

:-
   see('Sample.pro'),
   read(C1),
   seen,
   total(C1,R1),
   format('total = ~w~n',[R1]),
   cut(C1,C2),
   total(C2,R2),
   format('cut = ~w~n',[R2]),
   depth(C1,R3),
   format('depth = ~w~n',[R3]).
   
 :- halt.
 
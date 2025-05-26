

function [x l u] = init(N,pd) % Function for initialization


% Lower bounds and upper bounds
% l = [0.3 0.001 1];
% u = [0.6 0.02 3];

l=[0.2 0.0001 0.5];
u=[0.8 0.1 3];

for i = 1:N % Generation of initial solutions (position of crows)
    for j = 1:pd
        x(i,j) = l(j)+(u(j)-l(j)).*rand; % Position of the crows in the space
    end
end

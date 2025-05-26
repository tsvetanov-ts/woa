%   
function E = ga_error(par)

global t t0 tf X0 S0 V0 Xd X Xmod1 Sd S Smod1 V T mu k k1 Ex

p = size(par, 1);

mu = par(:, 1);
k  = par(:, 2); 
k1 = par(:, 3);

% options = simset('solver', 'ode45', 'RelTol', 1e-4, 'AbsTol', 1e-6, 'MaxStep', 1);
TIMESPAN = [t(1) t(length(t))];

[T, Xa, Y1, Y2] = sim('model_xs',  TIMESPAN, [], []);

Xmod1 = Y1; 
Smod1 = Y2; 

Xmod1 = interp1(T, Xmod1, t);
Smod1 = interp1(T, Smod1, t);

X = rep(Xd, [1 p]); 
S = rep(Sd, [1 p]);

Ex = Xmod1 - X; 
Es = Smod1 - S; 

E = sum(Ex.*Ex) + sum(Es.*Es);


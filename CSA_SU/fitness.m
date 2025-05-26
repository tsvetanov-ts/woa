
%function ft = fitness(xn,N,pd) % Function for fitness evaluation
function ft = fitness(xn) % Function for fitness evaluation


global td t0 tf X0 S0 V0 Xd X Xmod Sd Smod Ff V So T mu k k1 

load ec1807kf
load ec1807k
load ec1807ac
load ec1807gs

 Xd = ec1807kf(:, 3);
 Sd = ec1807kf(:, 2);
  Ff = ec1807kf(:, 6);
  V = ec1807k(:, 6);

% for X, S, F and V
  td = ec1807kf(:, 1);

ndx = find(ec1807kf(:, 1) >= 6.69 & ec1807kf(:, 1) <= 11.57);
 td = ec1807kf(ndx, 1);
Sd = ec1807kf(ndx, 2);
Xd = ec1807kf(ndx, 3); 
 Ff = ec1807kf(ndx, 6);
 V = ec1807k(ndx, 6);

t0 = td(1); 
tf = td(length(td));

So = 100;
X0 = Xd(1);
S0 = Sd(1);
V0 = V(1);

nd = find(ec1807kf(:, 1) >= 7.224 & ec1807kf(:, 1) <= 11.57);
Sd(nd) = 2*0.08 - ec1807kf(nd,2);

p = size(xn, 1);

mu = xn(:, 1)';
k  = xn(:, 2)'; 
k1 = xn(:, 3)';

% options = simset('solver', 'ode45', 'RelTol', 1e-4, 'AbsTol', 1e-6, 'MaxStep', 1);
TIMESPAN = [td(1) td(length(td))];

[T, Xa, Y1, Y2] = sim('model_xs',  TIMESPAN, [], []);
fprintf('get func details model call\n')
Xmod1 = Y1; 
Smod1 = Y2; 

Xmod1 = interp1(T, Xmod1, td);
Smod1 = interp1(T, Smod1, td);

X = rep(Xd, [1 p]); 
S = rep(Sd, [1 p]);

Ex = Xmod1 - X; 
Es = Smod1 - S; 

ft = sum(Ex.*Ex) + sum(Es.*Es);
clear all
close all

global t tac tdo Xd Sd Ad F V Mmax K1 K Ks N K2 E K3 Kla DOo

load ec1807kf
load ec1807k
load ec1807ac
load ec1807gs

Xd = ec1807kf(:,3);
Sd = ec1807kf(:,2);
A = ec1807ac(:,2);
F = ec1807kf(:,6);
V = ec1807k(:,6);
DOd = ec1807gs(:,4);%O2

t = ec1807kf(:,1);
tac = ec1807ac(:,1);
tdo1 = ec1807gs(:,1);

ndx = find(ec1807kf(:, 1) >= 6.69 & ec1807kf(:, 1)<= 11.57);
t = ec1807kf(ndx,1);
Sd = ec1807kf(ndx,2);
Xd = ec1807kf(ndx,3); 
F = ec1807kf(ndx,6);
V = ec1807k(ndx,6);

t0 = t(1); tf = t(length(t));

ndx1 = find(ec1807ac(:, 1) >= 6.69 & ec1807ac(:, 1)<= 11.57);
Adop = interp1(tac, A, [t0 tf]);
tac = tac(ndx1);
A = A(ndx1);
Ad = [Adop(1); A];
tac = [t0; tac];

ndx2 = find(ec1807gs(:, 1) >= 6.69 & ec1807gs(:, 1)<= 11.57);

O2dop = interp1(tdo1, DOd, [t0 tf]); 
tdo = tdo1(ndx2);
DOd = DOd(ndx2);
DOd = [O2dop(1); DOd; O2dop(2)];
tdo = [t0; tdo; tf];

X0 = Xd(1); S0 = Sd(1); A0 = Ad(1); DO0 = DOd(1); 






N = 40; % any even number
mf = 500; % max fun evals
fun = @error_sa_ta;
%rng default
lb = [0.2 0 0.5]; 
ub = [0.8 0.1 3]; 
x0 = [0.5 0.03 2];   % Monod

% options = optimoptions('surrogateopt','MaxFunctionEvaluations',mf);
% [xm,fvalm,~,~,pop] = surrogateopt(fun,lb,ub,options);

options = optimoptions("patternsearch",PlotFcn="psplotbestf");
options = optimoptions('patternsearch','MaxIterations',150);
[xm,fval] = patternsearch(fun,x0,[],[],[],[],lb,ub,[],options)
Mmax = xm(1);  Ks = xm(2); K1 = xm(3); 

options = simset('solver', 'ode45', 'RelTol', 1e-4, 'AbsTol', 1e-6, 'MaxStep', 1);
TIMESPAN = [t(1) t(length(t))];

[T, X, Y] = sim('Model_XS1',  TIMESPAN, options, []); % model 1

tmod1 = T; Xmod1 = Y(:, 1); Smod1 = Y(:, 2); 



Tt1 = [t0 6.7 6.8 6.9 7 7.3 7.6 7.9 8 8.3 8.6 8.9 9 9.3 9.6 9.9 10 10.3 10.6 11.2 11.4];
Xd = interp1(t, Xd, Tt1);
Sd = interp1(t, Sd, Tt1);

figure(1)
set(findall(gcf,'-property','FontSize'),'FontSize', 14)
plot(Tt1, Xd, 'r*','LineWidth', 2), grid, hold on, 
plot(tmod1, Xmod1, 'b','LineWidth', 2)
legend('exp. data', 'model data', 'Location', 'northwest')
title('Results from optimization'), xlabel('Time, [h]'), ylabel('Biomass, [g/l]')

figure(2)
set(findall(gcf,'-property','FontSize'),'FontSize', 14)
plot(Tt1, Sd, 'r*','LineWidth', 2), grid, hold on, 
plot(tmod1, Smod1, 'b','LineWidth', 2)
legend('exp. data', 'model data')
title('Results from optimization'), xlabel('Time, [h]'), ylabel('Substrate, [g/l]')



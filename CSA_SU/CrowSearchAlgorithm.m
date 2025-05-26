% -------------------------------------------------
% Citation details:
% Alireza Askarzadeh, Anovel metaheuristic method for solving constrained
% engineering optimization problems: Crow search algorithm, Computers &
% Structures, Vol. 169, 1-12, 2016.

% Programmed by Alireza Askarzadeh at Kerman Graduate %
% University of Advanced Technology (KGUT) %
% Date of programming: September 2015 %
% -------------------------------------------------
% This demo only implements a standard version of CSA for minimization of
% a standard test function (Sphere) on MATLAB 7.6.0 (R2008a).
% -------------------------------------------------
% Note:
% Due to the stochastic nature of meta-heuristc algorithms, different runs
% may lead to slightly different results.
% -------------------------------------------------
%function [bestsol,fval]=crow_search(time)
format long; %close all; 
clear all; clc

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


runtime = 30;%/*Algorithm can be run many times in order to see its robustness*/

ttt = cputime;

% time = 1;  %200
tmax = 100; % Maximum number of iterations (itermax)


pd = 3;    % Problem dimension (number of decision variables)
N = 100;    % Flock (population) size
AP = 0.1;  % Awareness probability 0.1
fl = 2;    % Flight length (fl) 2

Convergence = zeros(runtime, tmax);

for run = 1:runtime

[x l u] = init(N, pd); % Function for initialization
 
xn = x;
% save init_pop xn

ft = fitness(xn); % Function for fitness evaluation

mem = x; % Memory initialization

fit_mem = ft; % Fitness of memory positions

for t = 1:tmax
    
    num = ceil(N*rand(1,N)); % Generation of random candidate crows for following (chasing)
    for i = 1:N
        if rand > AP
            xnew(i,:)= x(i,:) + fl*rand*(mem(num(i),:) - x(i,:)); % Generation of a new position for crow i (state 1)
        else
            for j = 1:pd
                xnew(i,j) = l(j)+(l(j)-u(j))*rand; % Generation of a new position for crow i (state 2)
            end
        end
    end
    
    %current iteration
    t
    xn = xnew;
    ft = fitness(xn); % Function for fitness evaluation of new solutions

    for i=1:N % Update position and memory
        if xnew(i,:) >= l & xnew(i,:) <= u
            x(i,:) = xnew(i,:); % Update position
            if ft(i) < fit_mem(i)
                mem(i,:) = xnew(i,:); % Update memory
                fit_mem(i) = ft(i);
            end
        end
    end

    ffit(t) = min(fit_mem); % Best found value until iteration t
    fmin = min(fit_mem);
    GlobalMins(t) = fmin;
    indx = find(fit_mem == min(fit_mem));
    GlobalPar(:, t) = mem(indx(1), :);
    
    Convergence(run, t) = fmin;
end


fmin
ngbest = find(fit_mem == min(fit_mem));
g_best = mem(ngbest(1),:) % Solution of the problem

W = cputime - ttt;
par = g_best';

%current run
run
Mins_all(run) = fmin;

Pars_all(:, run) = par;
Ws(run) = W;
Results_all = [Pars_all' Mins_all' Ws'];

end

% save results_ga-csa_30_9 Results_all
save results_csa_1 Results_all

save Convergence_csa Convergence

mu = g_best(1); k = g_best(2); k1 = g_best(3); 
options = simset('solver', 'ode45', 'RelTol', 1e-4, 'AbsTol', 1e-6, 'MaxStep', 1);
TIMESPAN = [td(1) td(length(td))];

[T, X, Y] = sim('model_xs',  TIMESPAN, options, []);

tmod = T;
Xmod = Y(:, 1); 
Smod = Y(:, 2);

Tt1 = [t0 6.7 6.8 6.9 7 7.3 7.6 7.9 8 8.3 8.6 8.9 9 9.3 9.6 9.9 10 10.3 10.6 11.2 11.4];
Xd = interp1(td, Xd, Tt1);
Sd = interp1(td, Sd, Tt1);

figure(1)
set(findall(gcf,'-property','FontSize'),'FontSize', 14)
plot(Tt1, Xd, 'r*','LineWidth', 2), grid, hold on, 
plot(tmod, Xmod, 'b','LineWidth', 2)
legend('exp. data', 'model data', 'Location', 'northwest')
title('Results from optimization'), xlabel('Time, [h]'), ylabel('Biomass, [g/l]')

figure(2)
set(findall(gcf,'-property','FontSize'),'FontSize', 14)
plot(Tt1, Sd, 'r*','LineWidth', 2), grid, hold on, 
plot(tmod, Smod, 'b','LineWidth', 2)
legend('exp. data', 'model data')
title('Results from optimization'), xlabel('Time, [h]'), ylabel('Substrate, [g/l]')

figure(3)
set(findall(gcf,'-property','FontSize'),'FontSize', 14)
plot(1:tmax,Convergence(1,:), 'r'), hold on, 
plot(1:tmax,Convergence(2,:), 'b'), 
legend('Run 1', 'Run 2')
title('Convergence curve'), xlabel('Iterations'), ylabel('Objective function')
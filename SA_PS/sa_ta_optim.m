clear all
close all

global t tac tdo Xd Sd Ad F V Mmax K1 K Ks N K2 E K3 Kla DOo

load ec1807kf
load ec1807k
load ec1807ac
load ec1807gs

Xd = ec1807kf(:,3);
Sd = ec1807kf(:,2);
A  = ec1807ac(:,2);
F  = ec1807kf(:,6);
V  = ec1807k(:,6);
DOd= ec1807gs(:,4);%O2

t = ec1807kf(:,1);
tac = ec1807ac(:,1);
tdo1 = ec1807gs(:,1);

ndx = find(ec1807kf(:, 1) >= 6.69 & ec1807kf(:, 1)<= 11.57);
t  = ec1807kf(ndx,1);
Sd = ec1807kf(ndx,2);
Xd = ec1807kf(ndx,3);
F  = ec1807kf(ndx,6);
V  = ec1807k(ndx,6);

t0 = t(1); tf = t(length(t));

ndx1 = find(ec1807ac(:, 1) >= 6.69 & ec1807ac(:, 1)<= 11.57);

Adop = interp1(tac, A, [t0 tf]);
tac  = tac(ndx1);
A    = A(ndx1);
Ad   = [Adop(1); A];
tac  = [t0; tac];

ndx2 = find(ec1807gs(:, 1) >= 6.69 & ec1807gs(:, 1)<= 11.57);

O2dop = interp1(tdo1, DOd, [t0 tf]);
tdo   = tdo1(ndx2);
DOd   = DOd(ndx2);
DOd   = [O2dop(1); DOd; O2dop(2)];
tdo   = [t0; tdo; tf];

X0  = Xd(1); S0 = Sd(1); A0 = Ad(1); DO0 = DOd(1);

% opt X, S
% lb = [0.2 0 0.5 1]; ub = [0.8 0.1 3 10]; x0 = [0.5 0.03 2 3]; % Moser
% lb = [0.2 1 0.5]; ub = [0.8 20 3]; x0 = [0.5 6 2];           % Tessier
% lb = [0.2 0 0 0.5]; ub = [0.8 0.1 10 3]; x0 = [0.5 0.03 1 2];

% opt A
% Mmax = 0.5402; Ks = 0.0288; K1 = 2.0118; N=;
% lb = [0]; ub = [1]; x0 = [0.05];

% opt O2
% Mmax = 0.5402; Ks = 0.0288; K1 = 2.0118; K2=;
% lb = [0 0 19]; ub = [1 50 22]; x0 = [0.1 20 20];

% computational time
% wrap Simulated Annealing in multiple runs
NRUNS   = 30re;                  % number of independent SA starts
Results = zeros(NRUNS,5);     % [fval, Mmax, Ks, K1, cpu_time]

for run = 1:NRUNS
    tt = cputime;             % start timer

    % Simulated Annealing
    lb  = [0.2 0   0.5]; 
    ub  = [0.8 0.1 3]; 
    x0  = [0.5 0.03 2];        % Monod: [μmax, Ks, K1]
    options = optimoptions( ...
        'simulannealbnd', ...
        'PlotFcns', {@saplotbestf,@saplotf});
    options = optimoptions(options, ...
        'InitialTemperature', 100, ...
        'TemperatureFcn',    @temperatureexp, ...
        'ReannealInterval',  100, ...
        'DisplayInterval',   100);

    [par1, fval] = simulannealbnd(@error_sa_ta, x0, lb, ub, options);

    cpu_time = cputime - tt;    % record elapsed
    Results(run, :) = [fval, par1, cpu_time];
end

save('results_sa_ta.mat', 'Results');   % save all SA runs

% select best run (lowest SSE)
[~, best] = min(Results(:,1));
bestPar   = Results(best, 2:4);
Mmax      = bestPar(1);
Ks        = bestPar(2);
K1        = bestPar(3);

options  = simset('solver','ode45','RelTol',1e-4,'AbsTol',1e-6,'MaxStep',1);
TIMESPAN = [t(1) t(length(t))];

[T, X, Y] = sim('Model_XS1', TIMESPAN, options, []); % model 1
% [T, X, Y] = sim('model_xs2',  TIMESPAN, options, []); % model 2
% [T, X, Y] = sim('model_xs3',  TIMESPAN, options, []); % model 3

% [T, X, Y] = sim('model_xsa1', TIMESPAN, options, []); % model 1
% [T, X, Y] = sim('model_xsa2', TIMESPAN, options, []); % model 2
% [T, X, Y] = sim('model_xsa3', TIMESPAN, options, []); % model 3

% [T, X, Y] = sim('model_xsao1',TIMESPAN, options, []); % model 1
% [T, X, Y] = sim('model_xsao2',TIMESPAN, options, []); % model 2
% [T, X, Y] = sim('model_xsao3',TIMESPAN, options, []); % model 3

tmod1  = T;     Xmod1 = Y(:,1);   Smod1 = Y(:,2);
% Amod1 = Y(:,3);
% DOmod1 = Y(:,4);
...

figure(3), plot(t,   Xd,   'r*'), hold on, plot(tmod1, Xmod1, 'b')
title('Fed-batch cultivation process of E. coli MC4110'),
xlabel('Time, [h]'), ylabel('Biomass, [g/l]')

figure(4), plot(t,   Sd,   'r*'), hold on, plot(tmod1, Smod1, 'b')
title('Fed-batch cultivation process of E. coli MC4110'),
xlabel('Time, [h]'), ylabel('Substrate, [g/l]')

% figure(5), plot(tac, Ad,  'r*'), hold on, plot(tmod1, Amod1, 'b')
% %legend('Experimental data', 'Model data - SA', 2)
% title('Fed-batch cultivation process of E. coli MC4110'),
% xlabel('Time, [h]'), ylabel('Acetate, [g/l]')
%
% figure(6), plot(tdo, DOd, 'r*'), hold on, plot(tmod1, DOmod1, 'b')
% title('Fed-batch cultivation process of E. coli MC4110'),
% xlabel('Time, [h]'), ylabel('Dissolved oxygen, [%]')

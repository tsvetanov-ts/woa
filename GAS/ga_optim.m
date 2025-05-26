clear all
close all
clc

global t t0 tf X0 S0 V0 Xd X Xmod Sd Smod F V So T mu k k1 

load ec1807kf
load ec1807k
load ec1807ac
load ec1807gs

 Xd = ec1807kf(:, 3);
 Sd = ec1807kf(:, 2);
  F = ec1807kf(:, 6);
  V = ec1807k(:, 6);

% for X, S, F and V
  t = ec1807kf(:, 1);

ndx = find(ec1807kf(:, 1) >= 6.69 & ec1807kf(:, 1) <= 11.57);
 t = ec1807kf(ndx, 1);
Sd = ec1807kf(ndx, 2);
Xd = ec1807kf(ndx, 3); 
 F = ec1807kf(ndx, 6);
 V = ec1807k(ndx, 6);

t0 = t(1); 
tf = t(length(t));

So = 100;
X0 = Xd(1);
S0 = Sd(1);
V0 = V(1);

nd = find(ec1807kf(:, 1) >= 7.224 & ec1807kf(:, 1) <= 11.57);
Sd(nd) = 2*0.08 - ec1807kf(nd,2);

NRUNS = 30;
Results = zeros(NRUNS, 5);

%	Simple Genetic Algorithm

NIND = 400;	% Number of individuals per subpopulations
MAXGEN = 15;	% Maximal number of generations
NVAR = 3;	% No of variables
PRECI = 20;	% Precision of binary representation
MUTR = 0.2;	% Mutation rate
XOVR = 0.8; % Crossover rate
GGAP = 0.97;	% Generation gap, how many new individuals are created

SEL_F = 'rws';	% Name of selection function
XOV_F = 'xovdp';	% Name of recombination function for individuals
MUT_F = 'mut';	% Name of mutation function for individuals

% Build fielddescription matrix
FieldDR = [rep([PRECI], [1, NVAR]); [[0.2; 0.8], [0.0001; 0.1], [0.5; 3]]; rep([0;0;1;1], [1, NVAR])];
        
for run = 1:NRUNS

t1 = cputime;

% Create population
Chrom = crtbp(NIND, NVAR*PRECI);

% reset count variables
gen = 0;	% Counter	

% Evaluate Initial Population
ObjV = ga_error(bs2rv(Chrom, FieldDR))';

% Iterate population
while gen < MAXGEN
 
 	% Fitness assignement to whole population
	FitnV = ranking(ObjV);
            
	% Select individuals from population
	SelCh = select(SEL_F, Chrom, FitnV, GGAP);
     
	% Recombine selected individuals (crossover)
	SelCh = recombin(XOV_F, SelCh, XOVR);

	% Mutate offspring
	SelCh = mut(SelCh, MUTR);

	% Evaluate Population
	ObjVSel = ga_error(bs2rv(SelCh, FieldDR));

	% Insert offspring in population replacing parents
	[Chrom, ObjV] = reins(Chrom, SelCh, 1, 1, ObjV, ObjVSel');

	% Increment counter	
	gen = gen+1
	[y, i] = min(ObjV);
  	Error_min = min(ObjV)
	ERROR_MIN(gen) = Error_min;
	Par = bs2rv(Chrom(i, :), FieldDR)
	PAR(gen, :) = Par;
end

[Y, I] = min(ObjV);
ParOpt = bs2rv(Chrom(I, :), FieldDR);

Final_time = cputime-t1
Results(run, :) = [Error_min ParOpt Final_time];

end
save('results/results_exp_data.mat', 'Results')

%	End of Genetic Algorithm

mu = ParOpt(1); k = ParOpt(2); k1 = ParOpt(3);

options = simset('solver', 'ode45', 'RelTol', 1e-4, 'AbsTol', 1e-6, 'MaxStep', 1);
TIMESPAN = [t(1) t(length(t))];

[T, X, Y] = sim('model_xs',  TIMESPAN, options, []);

tmod = T;
Xmod = Y(:, 1); 
Smod = Y(:, 2);

Tt1 = [t0 6.7 6.8 6.9 7 7.3 7.6 7.9 8 8.3 8.6 8.9 9 9.3 9.6 9.9 10 10.3 10.6 11.2 11.4];
Xd = interp1(t, Xd, Tt1);
Sd = interp1(t, Sd, Tt1);

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

% figure(3)
% set(findall(gcf,'-property','FontSize'),'FontSize', 14)
% semilogy(1:gen, ERROR_MIN, 'b')
% title('Error Minimum through Generations'), xlabel('Generations'), ylabel('Error Minimum')
% hold on

%_________________________________________________________________________%
%  Whale Optimization Algorithm (WOA) source codes demo 1.0               %
%                                                                         %
%  Developed in MATLAB R2011b(7.13)                                       %
%                                                                         %
%  Author and programmer: Seyedali Mirjalili                              %
%                                                                         %
%         e-Mail: ali.mirjalili@gmail.com                                 %
%                 seyedali.mirjalili@griffithuni.edu.au                   %
%                                                                         %
%       Homepage: http://www.alimirjalili.com                             %
%                                                                         %
%   Main paper: S. Mirjalili, A. Lewis                                    %
%               The Whale Optimization Algorithm,                         %
%               Advances in Engineering Software , in press,              %
%               DOI: http://dx.doi.org/10.1016/j.advengsoft.2016.01.008   %
%                                                                         %
%_________________________________________________________________________%

% You can simply define your cost in a seperate file and load its handle to fobj 
% The initial parameters that you need are:
%__________________________________________
% fobj = @YourCostFunction
% dim = number of your variables
% Max_iteration = maximum number of generations
% SearchAgents_no = number of search agents
% lb=[lb1,lb2,...,lbn] where lbn is the lower bound of variable n
% ub=[ub1,ub2,...,ubn] where ubn is the upper bound of variable n
% If all the variables have equal lower bound you can just
% define lb and ub as two single number numbers

% To run WOA: [Best_score,Best_pos,WOA_cg_curve]=WOA(SearchAgents_no,Max_iteration,lb,ub,dim,fobj)

clear all 
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

runtime = 30;%/*Algorithm can be run many times in order to see its robustness*/
ttt = cputime;

%WOA %

SearchAgents_no=300; % Number of search agents

Function_name='F24';% Name of the test function that can be from F1 to F23 (Table 1,2,3 in the paper)

Max_iteration=30; % Maximum number of iterations

% Load details of the selected benchmark function
[lb,ub,dim,fobj]=Get_Functions_details(Function_name);

for run = 1:runtime
    [Best_score,Best_pos,WOA_cg_curve]=WOA(SearchAgents_no,Max_iteration,lb,ub,dim,fobj);

    W = cputime - ttt;
    par = Best_pos;

    run
    Mins_all(run) = Best_score;

    Pars_all(:, run) = par;
    Ws(run) = W;

    Convergence_woa(run, :) = WOA_cg_curve;

    Results_all = [Pars_all' Mins_all' Ws'];
% figure(22)
% set(findall(gcf, '-property', 'FontSize'), 'FontSize', 14)
% plot(1:Max_iteration, WOA_cg_curve, 'r')
% title('Convergance curve'), xlabel('Iterations'), ylabel('Objective function')
end



resultsfilename = "results_woa" + "_agents-" + SearchAgents_no + "_iters-" + Max_iteration + ".mat"
save(resultsfilename, 'Results_all')

convergencefilename = "convergence_woa" + "_agents-" + SearchAgents_no + "_iters-" + Max_iteration + ".mat"
save(convergencefilename, 'Convergence_woa')

%save resultsfilename Results_all
%save convergence_woa Convergence_woa

% figure('Position',[269   240   660   290])
% %Draw search space
% subplot(1,2,1);
% func_plot(Function_name);
% title('Parameter space')
% xlabel('x_1');
% ylabel('x_2');
% zlabel([Function_name,'( x_1 , x_2 )'])
% 
% %Draw objective space
% subplot(1,2,2);
% semilogy(WOA_cg_curve,'Color','r')
% title('Objective space')
% xlabel('Iteration');
% ylabel('Best score obtained so far');
% 
% axis tight
% grid on
% box on
% legend('WOA')

[err_min, i] = min(Results_all(:,4));
Best_par = Results_all(i,1:3);

ERROR_MIN = err_min

mu = Best_par(1); k = Best_par(2); k1 = Best_par(3); 
options = simset('solver', 'ode45', 'RelTol', 1e-4, 'AbsTol', 1e-6, 'MaxStep', 1);
TIMESPAN = [t(1) t(length(t))];

[T, X, Y] = sim('model_xs_woa',  TIMESPAN, options, []);

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

figure(3)
set(findall(gcf, '-property', 'FontSize'), 'FontSize', 14)
% TODO loop runtimes to plot all runs
plot(1:Max_iteration, Convergence_woa(1, :), 'r'), hold on,
plot(1:Max_iteration, Convergence_woa(2, :), 'b')
legend('Run 1', 'Run 2')
title('Convergance curve'), xlabel('Iterations'), ylabel('Objective function')


display(['The best solution obtained by WOA is : ', num2str(Best_pos)]);
display(['The best optimal value of the objective funciton found by WOA is : ', num2str(Best_score)]);


clear all
close all
clc

load ec1807kf
load ec1807k
load ec1807ac
load ec1807gs

 Xd = ec1807kf(:, 3);
 Sd = ec1807kf(:, 2);
  F = ec1807kf(:, 6);
  V = ec1807k(:, 6);
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

options = simset('solver', 'ode45', 'RelTol', 1e-4, 'AbsTol', 1e-6, 'MaxStep', 1);
TIMESPAN = [t(1) t(length(t))];

[T, X, Y] = sim('Model_XStest',  TIMESPAN, options, []);

tmod = T;
Xmod = Y(:, 1); 
Smod = Y(:, 2);

figure(1)
set(findall(gcf,'-property','FontSize'),'FontSize', 14)
plot(tmod, Xmod, 'b','LineWidth', 2), grid
legend('generated data', 'Location','northwest')
title('E. coli cultivation process'), 
xlabel('Time, [h]'), ylabel('Biomass, [g/l]')

figure(2)
set(findall(gcf,'-property','FontSize'),'FontSize', 14)
plot(tmod, Smod, 'b','LineWidth', 2), grid
legend('generated data')
title('E. coli cultivation process'), 
xlabel('Time, [h]'), ylabel('Substrate, [g/l]')

save generated_data tout tfeed Xout Sout Fout Vout

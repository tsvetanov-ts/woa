function gaPlot3D(fun,low,up,step)
%
% Matlab function for plotting the object function in 3D.
%
%  -----------------------------------------------------------------
%<<        Juha Haataja, Development Manager, Science Support       >>
%<<    Center for Scientific Computing, Box 405, FIN-02101 Espoo    >>
%<<           Phone +358 9 457 2731. Fax +358 9 457 2302            >>
%<<                 Internet: Juha.Haataja@csc.fi                   >>
%  -----------------------------------------------------------------

xr = low(1):step(1):up(1); 
yr = low(2):step(2):up(2);
[x y] = meshgrid(xr, yr);

% Compute the function values for the population elements.
z = eval([fun, '(x,y)']);

surface(xr,yr,z); xlabel('x'); ylabel('y'); 
hold on;
contour(xr,yr,z,15,'--'); 
axis([low(1) up(1) low(2) up(2)]); 
view(-35,25);
hold off;
